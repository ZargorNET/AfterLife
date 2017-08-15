package net.zargor.afterlife.web.pages;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import net.zargor.afterlife.web.IWebRequest;
import net.zargor.afterlife.web.PasswordEncrypt;
import net.zargor.afterlife.web.WebRequest;
import net.zargor.afterlife.web.WebServer;
import net.zargor.afterlife.web.objects.FullHttpReq;
import org.bson.Document;

import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by Zargor on 10.07.2017.
 */
@WebRequest(route = "/login", needToLogged = false, groupNeededRights = {})
public class Login implements IWebRequest {

    //TODO
    private boolean secure = false;


    @Override
    public DefaultFullHttpResponse onRequest(ChannelHandlerContext ctx, FullHttpReq req) throws Exception {
        if (req.getGroup() != null) {
            DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TEMPORARY_REDIRECT, Unpooled.EMPTY_BUFFER.retain());
            res.headers().set(HttpHeaderNames.LOCATION, "/dashboard");
            return res;
        }

        if (req.method() == HttpMethod.POST) {
            ResourceBundle resourceBundle = req.getClassResourceBundle("LoginClass");
            if (req.getPostAttributes().stream().anyMatch(attribute -> attribute.getName().equals("name")) && req.getPostAttributes().stream().anyMatch(attribute -> attribute.getName().equals("password"))) {
                String name = req.getPostAttributes().stream().filter(attribute -> attribute.getName().equals("name")).findFirst().orElse(null).getValue();
                String password = req.getPostAttributes().stream().filter(attribute -> attribute.getName().equals("password")).findFirst().orElse(null).getValue();

                if (name.isEmpty() || password.isEmpty()) {
                    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("incomplete_textboxes").getBytes(Charset.forName("UTF-8"))).retain());
                }
                if (!name.matches("[A-Za-z0-9\\-]+")) {
                    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(String.format(resourceBundle.getString("invalid_chars_username"), "A-Z a-z 0-9 -").getBytes(Charset.forName("UTF-8"))).retain());
                }
                if (!password.matches("[A-Za-z0-9\\-_#]+")) {
                    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(String.format(resourceBundle.getString("invalid_chars_password"), "A-Z a-z 0-9 - _ #").getBytes(Charset.forName("UTF-8"))).retain());
                }
                if (name.length() > 24 || password.length() > 100) {
                    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("too_many_chars").getBytes(Charset.forName("UTF-8"))).retain());
                }
                Document player = WebServer.getInstance().getMongoDB().getPlayerColl().find(new Document("name", name.toLowerCase())).first();

                if (player == null) {
                    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("unknown_user").getBytes(Charset.forName("UTF-8"))).retain());
                }
                if (!Objects.equals(player.getString("password"), new PasswordEncrypt().encryptPassword(password))) {
                    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("bad_password").getBytes(Charset.forName("UTF-8"))).retain());
                }
                if (player.getString("group") == null) {
                    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.copiedBuffer("Internal Server Error - 500".getBytes(Charset.forName("UTF-8"))).retain());
                }
                if (player.getString("banned") != null) {
                    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN, Unpooled.copiedBuffer(String.format(resourceBundle.getString("banned"), player.getString("banned")).getBytes(Charset.forName("UTF-8"))).retain());
                }
                DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.EMPTY_BUFFER.retain());
                res.headers().add(HttpHeaderNames.SET_COOKIE, WebServer.getInstance().getHandler().getSessionM().createCookieString(name, player.getString("group"), secure));
                res.headers().set(HttpHeaderNames.LOCATION, "/dashboard");
                return res;
            } else {
                return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("Bad POST parameters! Did you modify the parameters?".getBytes(Charset.forName("UTF-8"))).retain());
            }
        } else {
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("Please use POST method!".getBytes(Charset.forName("UTF-8"))).retain());
        }
    }
}