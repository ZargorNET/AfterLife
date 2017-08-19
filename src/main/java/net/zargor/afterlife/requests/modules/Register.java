package net.zargor.afterlife.requests.modules;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import net.zargor.afterlife.RecaptchaVerify;
import net.zargor.afterlife.WebServer;
import net.zargor.afterlife.mail.templates.RegisterConfirmationEmail;
import net.zargor.afterlife.objects.FullHttpReq;
import net.zargor.afterlife.requests.Module;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.Document;

import java.nio.charset.Charset;
import java.util.*;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;

/**
 * Created by Zargor on 11.07.2017.
 */
public class Register extends Module {

    //TODO
    private boolean secure = false;

    public Register() {
        super("register", true);
    }

    @Override
    public DefaultFullHttpResponse onModuleRequest(ChannelHandlerContext ctx, FullHttpReq req) throws Exception {
        if (req.getGroup() != null) {
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN, Unpooled.copiedBuffer("You're already logged in!".getBytes("UTF-8")).retain());
        }
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
            if (!RecaptchaVerify.verifyRecaptcha(recaptcha)) {
                return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("invalid_captcha").getBytes(Charset.forName("UTF-8"))).retain());
            }
            String code = generateCode();
            WebServer.getInstance().getMongoDB().getPlayerColl().insertOne(new Document("name", name).append("password", WebServer.getInstance().getPasswordEncrypt().getAlgorithm().hashPassword(password)).append("email", email).append("group", "default").append("registerConfirmationCode", code));
            RegisterConfirmationEmail mail = new RegisterConfirmationEmail(req.getLanguage(), name, code, WebServer.getInstance().getConfig().getValue("siteurl"), "Register confirmation", new Address[]{new InternetAddress(email)}, null, null);
            WebServer.getInstance().getMail().sendMail(mail);
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(resourceBundle.getString("success_send_confirmation").getBytes("UTF-8")));
        }
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("Invalid parameters!".getBytes(Charset.forName("UTF-8"))).retain());
    }

    private String generateCode() {
        return RandomStringUtils.random(16, "0123456789AaBbCcZz") + "_" + System.currentTimeMillis();
    }
}