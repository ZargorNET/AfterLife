package net.zargor.afterlife.server.requests.modules;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import net.zargor.afterlife.server.RecaptchaVerify;
import net.zargor.afterlife.server.WebServer;
import net.zargor.afterlife.server.mail.templates.RegisterConfirmationEmail;
import net.zargor.afterlife.server.objects.FullHttpReq;
import net.zargor.afterlife.server.requests.Module;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.bson.Document;

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
		ResourceBundle resourceBundle = this.getClassResourceBundle(req.getLanguage());
		if (req.getPostAttributes().stream().anyMatch(attribute -> attribute.getName().equals("name")) && req.getPostAttributes().stream().anyMatch(attribute -> attribute.getName().equals("password")) && req.getPostAttributes().stream().anyMatch(attribute -> attribute.getName().equals("email")) && req.getPostAttributes().stream().anyMatch(attribute -> attribute.getName().equals("recaptcha"))) {
			String name = req.getPostAttributes().stream().filter(attribute -> attribute.getName().equals("name")).findFirst().orElse(null).getValue().toLowerCase();
			String password = req.getPostAttributes().stream().filter(attribute -> attribute.getName().equals("password")).findFirst().orElse(null).getValue();
			String email = req.getPostAttributes().stream().filter(attribute -> attribute.getName().equals("email")).findFirst().orElse(null).getValue().toLowerCase();
			String recaptcha = req.getPostAttributes().stream().filter(attribute -> attribute.getName().equals("recaptcha")).findFirst().orElse(null).getValue();

			Map<String, String> values = new HashMap<>();

			if (name.isEmpty()) {
				values.put("name", "Name");
				return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(StrSubstitutor.replace(resourceBundle.getString("textbox_incomplete"), values).getBytes(Charset.forName("UTF-8"))).retain());
			}
			if (password.isEmpty()) {
				values.put("name", "Password");
				return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(StrSubstitutor.replace(resourceBundle.getString("textbox_incomplete"), values).getBytes(Charset.forName("UTF-8"))).retain());
			}
			if (email.isEmpty()) {
				values.put("name", "Email");
				return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(StrSubstitutor.replace(resourceBundle.getString("textbox_incomplete"), values).getBytes(Charset.forName("UTF-8"))).retain());
			}
			if (WebServer.getInstance().getMongoDB().getPlayerColl().find(new Document("name", name)).first() != null) {
				values.put("name", name);
				return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(StrSubstitutor.replace(resourceBundle.getString("already_exists"), values).getBytes(Charset.forName("UTF-8"))).retain());
			}
			if (WebServer.getInstance().getMongoDB().getPlayerColl().find(new Document("email", email)).first() != null) {
				values.put("name", email);
				return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(StrSubstitutor.replace(resourceBundle.getString("already_exists"), values).getBytes(Charset.forName("UTF-8"))).retain());
			}
			if (name.length() > 24) {
				values.put("name", "Name");
				values.put("max", String.valueOf(24));
				return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(StrSubstitutor.replace(resourceBundle.getString("textbox_too_many_chars"), values).getBytes(Charset.forName("UTF-8"))).retain());
			}
			if (password.length() > 24) {
				values.put("name", "Password");
				values.put("max", String.valueOf(24));
				return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(StrSubstitutor.replace(resourceBundle.getString("textbox_too_many_chars"), values).getBytes(Charset.forName("UTF-8"))).retain());
			}
			if (email.length() > 100) {
				values.put("name", "Email");
				values.put("max", String.valueOf(100));
				return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(StrSubstitutor.replace(resourceBundle.getString("textbox_too_many_chars"), values).getBytes(Charset.forName("UTF-8"))).retain());
			}
			if (name.length() < 4) {
				values.put("name", "Name");
				values.put("min", String.valueOf(4));
				return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(StrSubstitutor.replace(resourceBundle.getString("textbox_too_few_chars"), values).getBytes(Charset.forName("UTF-8"))).retain());
			}
			if (password.length() < 8) {
				values.put("name", "Password");
				values.put("min", String.valueOf(4));
				return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(StrSubstitutor.replace(resourceBundle.getString("textbox_too_few_chars"), values).getBytes(Charset.forName("UTF-8"))).retain());
			}
			if (!name.matches("[A-Za-z0-9\\-]+")) {
				values.put("name", "Name");
				values.put("chars", "A-Z a-z 0-9 -");
				return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("textbox_invalid_chars").getBytes(Charset.forName("UTF-8"))).retain());
			}
			if (!password.matches("[A-Za-z0-9\\-#]+")) {
				values.put("name", "Password");
				values.put("chars", "A-Z a-z 0-9 - #");
				return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("textbox_invalid_chars").getBytes(Charset.forName("UTF-8"))).retain());
			}
			if (!email.matches("[A-Za-z0-9_\\-.]+@{1}[A-Za-z0-9]+\\.{1}[A-Za-z]+")) {
				return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("textbox_invalid_email").getBytes(Charset.forName("UTF-8"))).retain());
			}
			if (!RecaptchaVerify.verifyRecaptcha(recaptcha)) {
				return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("incomplete_captcha").getBytes(Charset.forName("UTF-8"))).retain());
			}
			String code = generateCode();
			WebServer.getInstance().getMongoDB().getPlayerColl().insertOne(new Document("name", name).append("password", WebServer.getInstance().getPasswordEncrypt().getAlgorithm().hashPassword(password)).append("email", email).append("group", "default").append("registerConfirmationCode", code));
			RegisterConfirmationEmail mail = new RegisterConfirmationEmail(req.getLanguage(), name, code, WebServer.getInstance().getConfig().getValue("siteurl"), "Register confirmation", new Address[]{new InternetAddress(email)}, null, null);
			WebServer.getInstance().getMail().sendMail(mail);
			values.put("email", email.toLowerCase());
			return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(StrSubstitutor.replace(resourceBundle.getString("register_successfull"), values).getBytes("UTF-8")));
		}
		return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("Invalid parameters!".getBytes(Charset.forName("UTF-8"))).retain());
	}

	private String generateCode() {
		return RandomStringUtils.random(new Random().nextInt((16 - 6) + 1), "0123456789") + System.currentTimeMillis();
	}
}