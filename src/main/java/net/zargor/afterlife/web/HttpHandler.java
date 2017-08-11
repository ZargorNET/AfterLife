package net.zargor.afterlife.web;

import com.google.common.reflect.ClassPath;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.zargor.afterlife.web.exceptionhandlers.ThrowableFunction;
import net.zargor.afterlife.web.objects.FullHttpReq;
import net.zargor.afterlife.web.objects.Group;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.*;

/**
 * Created by Zargor on 07.07.2017.
 */
public class HttpHandler {

    private List<Class<IWebRequest>> classes = new ArrayList<>();
    @Getter
    private GroupManagement groupM = new GroupManagement();
    @Getter
    private SessionManagement sessionM = new SessionManagement();

    private String packagePath;

    public HttpHandler(String packagePath) {
        this.packagePath = packagePath;
        try {
            ClassPath.from(this.getClass().getClassLoader()).getTopLevelClasses(packagePath).stream()
                    .map((ThrowableFunction<ClassPath.ClassInfo, Class>) classInfo -> Class.forName(classInfo.getName()))
                    .filter(clazz -> clazz.isAnnotationPresent(WebRequest.class))
                    .filter(clazz -> Arrays.stream(clazz.getInterfaces()).anyMatch(i -> i == IWebRequest.class))
                    .forEach(classes::add);
        } catch (IOException exe) {
            exe.printStackTrace();
        }


        new Thread(() -> {
            while (true) {
                synchronized (tooManyRequestsListGet) {
                    tooManyRequestsListGet.removeAll(tooManyRequestsListGet.stream().filter(cra -> cra.time <= System.currentTimeMillis()).collect(Collectors.toList()));
                }
                synchronized (tooManyRequestsListOther) {
                    tooManyRequestsListOther.removeAll(tooManyRequestsListOther.stream().filter(cra -> cra.time <= System.currentTimeMillis()).collect(Collectors.toList()));
                }
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException exe) {
                    exe.printStackTrace();
                }
            }
        }, "RequestLimitThread");
    }

    public void fireRequestHandling(ChannelHandlerContext ctx, FullHttpRequest req) throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException {
        FullHttpResponse res;
        if (req.uri().contains(".")) {
            MimeTypes type = Arrays.stream(MimeTypes.values()).filter(v -> Arrays.stream(v.getEnding()).anyMatch(s -> req.uri().endsWith(s))).findFirst().orElse(null);
            if (type == null) {
                res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_ACCEPTABLE, Unpooled.EMPTY_BUFFER.retain());
                ctx.writeAndFlush(res).addListener(future -> {
                    ctx.close();
                    res.content().release();
                });
                return;
            }
            if (type == MimeTypes.HTML) {
                fireHTMLReq(req.uri(), ctx, req);
                return;
            }
            fireFileReq(req.uri(), type, ctx, req);
            return;
        }
        fireHTMLReq(req.uri(), ctx, req);
    }

    private void fireHTMLReq(String path, ChannelHandlerContext ctx, FullHttpRequest req) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        if (tooManyRequests(ctx, req.method())) {
            if (req.method() != HttpMethod.GET) {
                DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TOO_MANY_REQUESTS, Unpooled.copiedBuffer("Immer ruhig mit den Pferden! Du hast zu viele Anfragen geschickt. Bitte warte einen Moment!".getBytes(Charset.forName("UTF-8"))).retain());
                ctx.writeAndFlush(res).addListener(future -> {
                    ctx.close();
                    res.content().release();
                });
            } else {
                //TODO Richtige Seite einbinden:
                DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TOO_MANY_REQUESTS, Unpooled.copiedBuffer("Immer ruhig mit den Pferden! Du hast zu viele Anfragen geschickt. Bitte warte einen Moment!".getBytes(Charset.forName("UTF-8"))).retain());
                ctx.writeAndFlush(res).addListener(future -> {
                    ctx.close();
                    res.content().release();
                });
            }
            return;
        }
        Class<? extends IWebRequest> clazz = classes.stream().filter(c -> c.getDeclaredAnnotation(WebRequest.class).route().equals(path.contains("?") ? path.split("\\?")[0] : path)).findFirst().orElse(null);
        if (clazz == null)
            clazz = ((Class<? extends IWebRequest>) Class.forName(packagePath + ".NotFoundPage"));
        String lang = getLanguage(req);
        FullHttpReq newFullHttpReq = new FullHttpReq(req, lang);
        WebRequest anno = clazz.getDeclaredAnnotation(WebRequest.class);
        Group usersGroup = newFullHttpReq.getGroup();
        if (!clazz.getSimpleName().equals("NotFoundPage")) {
            if (anno.needToLogged()) {
                if ((usersGroup != null && !Arrays.stream(anno.groupNeededRights()).allMatch(usersGroup::hasPermission)) || usersGroup == null) {
                    if (req.method() == HttpMethod.GET) {
                        DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TEMPORARY_REDIRECT, Unpooled.EMPTY_BUFFER.retain());
                        res.headers().set(HttpHeaderNames.LOCATION, "/?needLogin");
                        ctx.writeAndFlush(res).addListener(future -> {
                            ctx.close();
                            res.content().release();
                        });
                        return;
                    } else {
                        DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TEMPORARY_REDIRECT, Unpooled.copiedBuffer("Du musst dich erst einloggen!".getBytes(Charset.forName("UTF-8"))).retain());
                        res.headers().set(HttpHeaderNames.LOCATION, "/?needLogin");
                        ctx.writeAndFlush(res).addListener(future -> {
                            ctx.close();
                            res.content().release();
                        });
                    }
                }
            }
        }
        DefaultFullHttpResponse res = (DefaultFullHttpResponse) clazz.getMethod("onRequest", ChannelHandlerContext.class, FullHttpReq.class).invoke(clazz.newInstance(), ctx, newFullHttpReq);
        if (!lang.equals(newFullHttpReq.getLanguage()))
            res.headers().add(HttpHeaderNames.SET_COOKIE, String.format("lang=%s; Expires=%s", newFullHttpReq.getLanguage(), DateUtils.addYears(new Date(), 1).toGMTString()));
        res.headers().set(HttpHeaderNames.CONTENT_TYPE, MimeTypes.HTML.getMimeText());
        res.headers().set(HttpHeaderNames.CONTENT_LENGTH, res.content().array().length);
        res.headers().set(HttpHeaderNames.CACHE_CONTROL, "no-store, must-revalidate");
        ctx.writeAndFlush(res).addListener(future -> {
            if (res.content().isReadable())
                res.content().release();
            ctx.close();
        });
    }

    private void fireFileReq(String path, MimeTypes type, ChannelHandlerContext ctx, FullHttpRequest req) throws IOException {
        byte[] bytes = readResourceFile(path);
        if (bytes == null) {
            DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, Unpooled.EMPTY_BUFFER.retain());
            ctx.writeAndFlush(res).addListener(future -> {
                res.content().release();
                ctx.close();
            });
            return;
        }
        ByteBuf byteBuf = Unpooled.copiedBuffer(bytes).retain();
        DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, byteBuf);
        res.headers().set(HttpHeaderNames.CONTENT_TYPE, type.getMimeText());
        res.headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
        res.headers().set(HttpHeaderNames.CACHE_CONTROL, type != MimeTypes.CSS ? "max-age=604800" : "no-store, must-revalidate");
        ctx.writeAndFlush(res).addListener(future -> {
            byteBuf.release();
            ctx.close();
        });
    }

    @Data
    @AllArgsConstructor
    private class ConnectionRequestAmount {

        private String ip;
        private long time;
        private int amount;
    }

    private final List<ConnectionRequestAmount> tooManyRequestsListGet = new ArrayList<>();
    private final List<ConnectionRequestAmount> tooManyRequestsListOther = new ArrayList<>();

    /**
     * Returns true if user has made too many requests
     */
    private boolean tooManyRequests(ChannelHandlerContext ctx, HttpMethod method) {
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getHostString();
        if (method == HttpMethod.GET) {
            synchronized (tooManyRequestsListGet) {
                ConnectionRequestAmount con = tooManyRequestsListGet.stream().filter(cra -> Objects.equals(cra.getIp(), ip)).findFirst().orElse(null);
                if (con == null) {
                    con = new ConnectionRequestAmount(ip, System.currentTimeMillis() + 1000 * 60, 0);
                    tooManyRequestsListGet.add(con);
                }
                con.amount++;
                if (con.time <= System.currentTimeMillis()) {
                    tooManyRequestsListGet.remove(con);
                    return false;
                }
                return con.amount >= 100;
            }
        } else {
            synchronized (tooManyRequestsListOther) {
                ConnectionRequestAmount con = tooManyRequestsListOther.stream().filter(cra -> Objects.equals(cra.getIp(), ip)).findFirst().orElse(null);
                if (con == null) {
                    con = new ConnectionRequestAmount(ip, System.currentTimeMillis() + 1000 * 60, 0);
                    tooManyRequestsListOther.add(con);
                }
                con.amount++;
                if (con.time <= System.currentTimeMillis()) {
                    tooManyRequestsListOther.remove(con);
                    return false;
                }
                return con.amount >= 20;
            }
        }
    }

    /**
     * Reads a resource
     *
     * @param path The path
     * @return The bytes or -1 if file is null
     */
    private byte[] readResourceFile(String path) throws IOException {
        InputStream in = this.getClass().getResourceAsStream(path);
        if (in == null)
            return null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(in, out);
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);
        return out.toByteArray();
    }

    /**
     * @return Language from cookie else "en"
     */
    private String getLanguage(FullHttpRequest req) {
        String langFrom = req.headers().get(HttpHeaderNames.ACCEPT_LANGUAGE);
        if (langFrom == null)
            langFrom = "en";
        else
            langFrom = langFrom.substring(0, 2);

        if (req.headers().get(HttpHeaderNames.COOKIE) == null)
            return langFrom;
        Cookie langCookie = ServerCookieDecoder.STRICT.decode(req.headers().get(HttpHeaderNames.COOKIE)).stream().filter(cookie -> cookie.name().equals("lang")).findFirst().orElse(null);
        return langCookie != null ? langCookie.value() : langFrom;
    }

}
