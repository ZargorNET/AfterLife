package net.zargor.afterlife.requests.modules;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.Attribute;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.zargor.afterlife.RecaptchaVerify;
import net.zargor.afterlife.WebServer;
import net.zargor.afterlife.exceptionhandlers.ThrowableFunction;
import net.zargor.afterlife.mail.templates.PasswordresetMail;
import net.zargor.afterlife.objects.FullHttpReq;
import net.zargor.afterlife.requests.Module;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.bson.Document;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;


public class Passwordreset extends Module {

    public static final int RESET_TIME = 45;
    @Getter
    private final List<PasswordresetCode> codes;

    public Passwordreset() {
        super("passwordreset", true);
        codes = new ArrayList<>();

        new Thread(() -> {
            while (true) {
                synchronized (codes) {
                    Date d = new Date();

                    codes.removeAll(
                            codes.stream()
                                    .filter(passwordresetCode -> passwordresetCode.expireTime < d.getTime())
                                    .filter(passwordresetCode -> DateUtils.addMinutes(new Date(passwordresetCode.getExpireTime()), RESET_TIME).getTime() <= d.getTime())
                                    .collect(Collectors.toList()));

                }
                try {
                    Thread.sleep(TimeUnit.MINUTES.toMillis(1));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "Passwordreset code cleaner").start();
    }

    @Override
    public DefaultFullHttpResponse onModuleRequest(ChannelHandlerContext ctx, FullHttpReq req) throws Exception {
        if (req.getGroup() != null) {
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN, Unpooled.copiedBuffer("You're already logged in!".getBytes("UTF-8")).retain());
        }
        ResourceBundle resourceBundle = req.getClassResourceBundle("PasswordResetClass");
        String email = req.getPostAttributes().stream().filter(attribute -> attribute.getName().equalsIgnoreCase("email")).map((ThrowableFunction<? super Attribute, String>) Attribute::getValue).findFirst().orElse(null);
        String recaptcha = req.getPostAttributes().stream().filter(attribute -> attribute.getName().equalsIgnoreCase("recaptcha")).map((ThrowableFunction<? super Attribute, String>) Attribute::getValue).findFirst().orElse(null);
        String code = req.getPostAttributes().stream().filter(attribute -> attribute.getName().equalsIgnoreCase("code")).map((ThrowableFunction<? super Attribute, String>) Attribute::getValue).findFirst().orElse(null);
        if (code != null) {
            return reedemCode(ctx, req, resourceBundle, code);
        }
        if (email == null)
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("email_null").getBytes("UTF-8")));
        if (recaptcha == null)
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("recaptcha_null").getBytes("UTF-8")));
        if (email.length() > 100)
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(String.format(resourceBundle.getString("email_too_long"), 100).getBytes("UTF-8")));
        Document document = WebServer.getInstance().getMongoDB().getPlayerColl().find(Filters.eq("email", email)).first();
        if (document == null)
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("user_unknown").getBytes("UTF-8")));
        if (!RecaptchaVerify.verifyRecaptcha(recaptcha))
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("recaptcha_null").getBytes("UTF-8")));

        return sendCode(ctx, req, resourceBundle, document);

    }

    private DefaultFullHttpResponse sendCode(ChannelHandlerContext ctx, FullHttpReq req, ResourceBundle bundle, Document doc) throws Exception {
        synchronized (codes) {
            PasswordresetCode pc = codes.stream().filter(pc1 -> pc1.username.equalsIgnoreCase(doc.getString("name"))).findFirst().orElse(null);
            if (pc != null) {
                int remainingTime = new Date(DateUtils.addMinutes(new Date(pc.getExpireTime()), RESET_TIME).getTime() - new Date().getTime()).getMinutes();
                if (remainingTime == 0)
                    remainingTime = 1;
                return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(String.format(bundle.getString("too_fast_request"), remainingTime).getBytes("UTF-8")));
            }
            String code = generateCode();
            PasswordresetMail mail = new PasswordresetMail(req.getLanguage(), doc.getString("name"), code, WebServer.getInstance().getConfig().getValue("siteurl"), "Passwordreset", new Address[]{new InternetAddress(doc.getString("email"))}, null, null);
            WebServer.getInstance().getMail().sendMail(mail);
            codes.add(new PasswordresetCode(doc.getString("name"), code));
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(bundle.getString("success_send").getBytes("UTF-8")));

        }
    }

    private DefaultFullHttpResponse reedemCode(ChannelHandlerContext ctx, FullHttpReq req, ResourceBundle bundle, String code) throws Exception {
        synchronized (codes) {
            String password = req.getPostAttributes().stream().filter(attribute -> attribute.getName().equalsIgnoreCase("password")).map((ThrowableFunction<? super Attribute, String>) Attribute::getValue).findFirst().orElse(null);
            PasswordresetCode passwordresetCode = codes.stream().filter(pc -> pc.code.equals(code)).findFirst().orElse(null);
            if (password == null)
                return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("Bad request! Did you modify the post parameters?".getBytes("UTF-8")));
            if (passwordresetCode == null || passwordresetCode.getExpireTime() < new Date().getTime() || !passwordresetCode.isValid())
                return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(bundle.getString("code_invalid").getBytes("UTF-8")));
            if (password.length() < 8)
                return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(String.format(bundle.getString("password_too_short"), 8).getBytes("UTF-8")));
            if (password.length() > 24)
                return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("Password ist too long! Did you modify the page?".getBytes("UTF-8")));
            if (!password.matches("[A-Za-z0-9\\-#]+"))
                return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(bundle.getString("password_invalid_chars").getBytes("UTF-8")));
            WebServer.getInstance().getMongoDB().getPlayerColl().updateOne(Filters.eq("name", passwordresetCode.username), Updates.set("password", WebServer.getInstance().getPasswordEncrypt().getAlgorithm().hashPassword(password)));
            passwordresetCode.setValid(false);
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.EMPTY_BUFFER);

        }
    }

    @EqualsAndHashCode
    @Getter
    public class PasswordresetCode {

        private final String username;
        private final String code;
        private long expireTime;
        @Setter
        private boolean valid = true;

        public PasswordresetCode(String username, String code) {
            this.username = username;
            this.code = code;
            expireTime = DateUtils.addMinutes(new Date(), 15).getTime();
        }
    }

    private String generateCode() {
        return RandomStringUtils.random(16, "0123456789") + "_" + System.currentTimeMillis();
    }
}
