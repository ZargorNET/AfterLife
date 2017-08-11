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
import net.zargor.afterlife.web.RecaptchaVerify;
import net.zargor.afterlife.web.WebRequest;
import net.zargor.afterlife.web.WebServer;
import net.zargor.afterlife.web.objects.FullHttpReq;
import org.bson.Document;

import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by Zargor on 11.07.2017.
 */
@WebRequest(route = "/register", needToLogged = false, groupNeededRights = {})
public class Register implements IWebRequest {

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
            ResourceBundle resourceBundle = req.getClassResourceBundle("RegisterClass");
            if (req.getPostAttributes().stream().anyMatch(attribute -> attribute.getName().equals("name")) && req.getPostAttributes().stream().anyMatch(attribute -> attribute.getName().equals("password")) && req.getPostAttributes().stream().anyMatch(attribute -> attribute.getName().equals("email")) && req.getPostAttributes().stream().anyMatch(attribute -> attribute.getName().equals("recaptcha"))) {
                String name = req.getPostAttributes().stream().filter(attribute -> attribute.getName().equals("name")).findFirst().orElse(null).getValue().toLowerCase();
                String password = req.getPostAttributes().stream().filter(attribute -> attribute.getName().equals("password")).findFirst().orElse(null).getValue();
                String email = req.getPostAttributes().stream().filter(attribute -> attribute.getName().equals("email")).findFirst().orElse(null).getValue().toLowerCase();
                String recaptcha = req.getPostAttributes().stream().filter(attribute -> attribute.getName().equals("recaptcha")).findFirst().orElse(null).getValue();

                if (name.isEmpty() || password.isEmpty() || email.isEmpty()) {
                    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("incomplete_textboxes").getBytes(Charset.forName("UTF-8"))).retain());
                }
                if (WebServer.getInstance().getMongoDB().getPlayerColl().find(new Document("name", name)).first() != null) {
                    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("account_exists").getBytes(Charset.forName("UTF-8"))).retain());
                }
                if (WebServer.getInstance().getMongoDB().getPlayerColl().find(new Document("email", email)).first() != null) {
                    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("email_exists").getBytes(Charset.forName("UTF-8"))).retain());
                }
                if (name.length() > 24 || password.length() > 24 || email.length() > 100) {
                    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("Parameters are too long!".getBytes(Charset.forName("UTF-8"))).retain());
                }
                if (name.length() < 4) {
                    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(String.format(resourceBundle.getString("name_too_short"), 4).getBytes(Charset.forName("UTF-8"))).retain());
                }
                if (password.length() < 8) {
                    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(String.format(resourceBundle.getString("password_too_short"), 8).getBytes(Charset.forName("UTF-8"))).retain());
                }
                if (!name.matches("[A-Za-z0-9\\-]+")) {
                    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("name_invalid_chars").getBytes(Charset.forName("UTF-8"))).retain());
                }
                if (!password.matches("[A-Za-z0-9\\-#]+")) {
                    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("password_invalid_chars").getBytes(Charset.forName("UTF-8"))).retain());
                }
                if (!email.matches("[A-Za-z0-9_\\-.]+@{1}[A-Za-z0-9]+\\.{1}[A-Za-z]+")) {
                    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("email_invalid_chars").getBytes(Charset.forName("UTF-8"))).retain());
                }
                if (!new RecaptchaVerify().verifyRecaptcha(recaptcha)) {
                    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("invalid_captcha").getBytes(Charset.forName("UTF-8"))).retain());
                }
                WebServer.getInstance().getMongoDB().getPlayerColl().insertOne(new Document("name", name).append("password", new PasswordEncrypt().encryptPassword(password)).append("email", email).append("group", "default"));
                DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.EMPTY_BUFFER.retain());
                res.headers().add(HttpHeaderNames.SET_COOKIE, WebServer.getInstance().getHandler().getSessionM().createCookieString(name, "default", secure));
                res.headers().set(HttpHeaderNames.LOCATION, "/dashboard");
                return res;
            }
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("Invalid parameters!".getBytes(Charset.forName("UTF-8"))).retain());
        } else {
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("Please use POST method!".getBytes(Charset.forName("UTF-8"))).retain());
        }
    }
}