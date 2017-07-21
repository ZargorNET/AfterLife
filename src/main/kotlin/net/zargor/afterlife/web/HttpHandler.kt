package net.zargor.afterlife.web

import com.google.common.reflect.ClassPath
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.cookie.Cookie
import io.netty.handler.codec.http.cookie.ServerCookieDecoder
import net.zargor.afterlife.web.objects.GroupPermissions
import org.apache.commons.io.IOUtils
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.InetSocketAddress

/**
 * Created by Zargor on 07.07.2017.
 */
class HttpHandler(val webServer : WebServer, val packagePath : String) {
    private val classes : MutableList<Class<in IWebRequest>> = arrayListOf()
    val sessionM = SessionManagement(webServer)
    val groupM = GroupManagement(webServer)

    init {
        ClassPath.from(webServer.javaClass.classLoader).getTopLevelClassesRecursive(packagePath)
                .map { Class.forName(it.name) }
                .filter { clazz -> clazz.isAnnotationPresent(WebRequest::class.java) && clazz.interfaces.any { it == IWebRequest::class.java } }
                .mapTo(classes) { it as Class<in IWebRequest> }
    }

    @Throws(Exception::class)
    fun fireRequestHandling(ctx : ChannelHandlerContext, req : FullHttpRequest) {
        var res : FullHttpResponse? = null
        if (req.uri().contains(".", true)) {
            val type : MimeTypes? = MimeTypes.values().filter { v -> v.endings.any { req.uri().endsWith(it) } }.getOrNull(0)
            if (type == null) {
                res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_ACCEPTABLE, Unpooled.EMPTY_BUFFER)
                ctx.writeAndFlush(res)
                ctx.close()
                return
            }
            if (type == MimeTypes.HTML) {
                fireHTMLReq(req.uri(), ctx, req)
                return
            }
            fireFileReq(req.uri(), type, ctx, req)
            return
        }
        fireHTMLReq(req.uri(), ctx, req)
    }

    private fun fireHTMLReq(path : String, ctx : ChannelHandlerContext, req : FullHttpRequest) {
        if (tooManyRequests(ctx, req.method())) {
            if (req.method() != HttpMethod.GET) {
                val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TOO_MANY_REQUESTS, Unpooled.copiedBuffer("Immer ruhig mit den Pferden! Du hast zu viele Anfragen geschickt. Bitte warte einen Moment!".toByteArray(Charsets.UTF_8)).retain())
                ctx.writeAndFlush(res).addListener { ctx.close();res.content().release(); }
            } else {
                //TODO Richtige Seite einbinden:
                val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TOO_MANY_REQUESTS, Unpooled.copiedBuffer("Immer ruhig mit den Pferden! Du hast zu viele Anfragen geschickt. Bitte warte einen Moment!".toByteArray(Charsets.UTF_8)).retain())
                ctx.writeAndFlush(res).addListener { ctx.close();res.content().release(); }
            }
            return
        }
        var clazz : Class<in IWebRequest>? = classes.filter { it.getDeclaredAnnotation(WebRequest::class.java).route == (if (path.contains('?')) path.split('?')[0] else path) }.getOrNull(0)
        if (clazz == null) {
            clazz = Class.forName(packagePath + ".404Page") as Class<in IWebRequest>
        }
        val anno : WebRequest = clazz.getDeclaredAnnotation(WebRequest::class.java)
        var logged : GroupPermissions = GroupPermissions.NONE
        val cookie : Cookie? = if (!cookieSet.isEmpty()) cookieSet.filter { it.name() == "z-sID" }.getOrNull(0) else null
        if (cookie != null) {
            val session = sessionM.getSessionsByID(cookie.value())
            if (session != null) {
                logged = session.group
            }
        }

        if (anno.permission != GroupPermissions.NONE && clazz.simpleName != "404Page") {
            val perm = anno.permission
            if (logged.ordinal < perm.ordinal) {
                val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.MOVED_PERMANENTLY, Unpooled.EMPTY_BUFFER.retain())
                res.headers().set(HttpHeaderNames.LOCATION, "/?needLogin")
                ctx.writeAndFlush(res).addListener { ctx.close();res.content().release(); }
                return
            }
        }
        val args : Map<String, String>? = splitArguments(path)
        val value = clazz.getMethod("onRequest", WebServer::class.java, ChannelHandlerContext::class.java, FullHttpRequest::class.java, Set::class.java, GroupPermissions::class.java, Map::class.java).invoke(clazz.newInstance(), webServer, ctx, req, cookieSet, logged, args)
        val res : DefaultFullHttpResponse = value as DefaultFullHttpResponse
        res.headers().set(HttpHeaderNames.CONTENT_TYPE, MimeTypes.HTML.mimeText)
        res.headers().set(HttpHeaderNames.CONTENT_LENGTH, res.content().array().size)
        res.headers().set(HttpHeaderNames.CACHE_CONTROL, "no-store, must-revalidate")
        ctx.writeAndFlush(res).addListener({ if (res.content().isReadable) res.content().release(); ctx.close() })
    }

    private fun fireFileReq(path : String, type : MimeTypes, ctx : ChannelHandlerContext, req : FullHttpRequest) {
        val bytes : ByteArray? = readResourceFile(path)
        if (bytes == null) {
            val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, Unpooled.EMPTY_BUFFER)
            ctx.writeAndFlush(res)
            ctx.close()
            return
        }
        val byteBuf : ByteBuf = Unpooled.copiedBuffer(bytes).retain()
        val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, byteBuf)
        res.headers().set(HttpHeaderNames.CONTENT_TYPE, type.mimeText)
        res.headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.size.toString())
        res.headers().set(HttpHeaderNames.CACHE_CONTROL, if (type != MimeTypes.CSS) "max-age=604800" else "no-store, must-revalidate")
        ctx.writeAndFlush(res).addListener({ byteBuf.release(); ctx.close() })
    }

    private data class ConnectionRequestAmount(val ip : String, var time : Long, var amount : Int)

    private val tooManyRequestsListGet = mutableListOf<ConnectionRequestAmount>()
    private val tooManyRequestsListOther = mutableListOf<ConnectionRequestAmount>()
    /**
     * Returns true if user has made too many requests
     */
    private fun tooManyRequests(ctx : ChannelHandlerContext, method : HttpMethod) : Boolean {
        val ip : String = (ctx.channel().remoteAddress() as InetSocketAddress).hostString
        if (method == HttpMethod.GET) {
            synchronized(tooManyRequestsListGet, {
                var con : ConnectionRequestAmount? = tooManyRequestsListGet.filter { it.ip == ip }.firstOrNull()
                if (con == null) {
                    con = ConnectionRequestAmount(ip, System.currentTimeMillis() + 1000 * 60, 1)
                    tooManyRequestsListGet.add(con)
                }
                con.amount += 1
                if (con.time <= System.currentTimeMillis()) {
                    tooManyRequestsListGet.remove(con)
                    return false
                }
                return con.amount >= 100
            })
        } else {
            synchronized(tooManyRequestsListOther, {
                var con : ConnectionRequestAmount? = tooManyRequestsListOther.filter { it.ip == ip }.firstOrNull()
                if (con == null) {
                    con = ConnectionRequestAmount(ip, System.currentTimeMillis() + 1000 * 60, 1)
                    tooManyRequestsListOther.add(con!!)
                }
                con!!.amount += 1
                if (con!!.time <= System.currentTimeMillis()) {
                    tooManyRequestsListOther.remove(con!!)
                    return false
                }
                return con!!.amount >= 20
            })
        }
    }

    init {
        Thread({
            while (true) {
                synchronized(tooManyRequestsListGet, {
                    tooManyRequestsListGet.filter { it.time <= System.currentTimeMillis() }.forEach { tooManyRequestsListGet.remove(it) }
                })
                synchronized(tooManyRequestsListOther, {
                    tooManyRequestsListOther.filter { it.time <= System.currentTimeMillis() }.forEach { tooManyRequestsListOther.remove(it) }
                })
                Thread.sleep(30000)
            }
        })
    }

    /**
     * Reads a resource
     * @param path The path
     * @return The bytes or -1 if file is null
     */
    private fun readResourceFile(path : String) : ByteArray? {
        val input : InputStream = javaClass.getResourceAsStream(path) ?: return null
        val output : ByteArrayOutputStream = ByteArrayOutputStream()
        IOUtils.copy(input, output)
        IOUtils.closeQuietly(input)
        IOUtils.closeQuietly(output)
        return output.toByteArray()
    }
}