package net.zargor.afterlife.server.requests.modules;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.Attribute;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.zargor.afterlife.server.RecaptchaVerify;
import net.zargor.afterlife.server.WebServer;
import net.zargor.afterlife.server.exceptionhandlers.ThrowableFunction;
import net.zargor.afterlife.server.mail.templates.PasswordresetMail;
import net.zargor.afterlife.server.objects.FullHttpReq;
import net.zargor.afterlife.server.requests.Module;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.commons.lang3.time.DateUtils;
import org.bson.Document;


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
		ResourceBundle resourceBundle = this.getClassResourceBundle(req.getLanguage());
		String email = req.getPostAttributes().stream().filter(attribute -> attribute.getName().equalsIgnoreCase("email")).map((ThrowableFunction<? super Attribute, String>) Attribute::getValue).findFirst().orElse(null);
		String recaptcha = req.getPostAttributes().stream().filter(attribute -> attribute.getName().equalsIgnoreCase("recaptcha")).map((ThrowableFunction<? super Attribute, String>) Attribute::getValue).findFirst().orElse(null);
		String code = req.getPostAttributes().stream().filter(attribute -> attribute.getName().equalsIgnoreCase("code")).map((ThrowableFunction<? super Attribute, String>) Attribute::getValue).findFirst().orElse(null);
		if (code != null) {
			return reedemCode(ctx, req, resourceBundle, code);
		}
		Map<String, String> values = new HashMap<>();
		if (email == null) {
			values.put("name", "Password");
			return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(StrSubstitutor.replace(resourceBundle.getString("textbox_incomplete"), values).getBytes("UTF-8")));
		}
		if (recaptcha == null) {
			return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("incomplete_captcha").getBytes("UTF-8")));
		}
		if (email.length() > 100) {
			values.put("name", "Email");
			return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(StrSubstitutor.replace(resourceBundle.getString("textbox_too_many_chars"), values).getBytes("UTF-8")));
		}
		Document document = WebServer.getInstance().getMongoDB().getPlayerColl().find(Filters.eq("email", email)).first();
		if (document == null) {
			values.put("name", email);
			return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(StrSubstitutor.replace(resourceBundle.getString("unknown_user"), values).getBytes("UTF-8")));
		}
		if (!RecaptchaVerify.verifyRecaptcha(recaptcha)) {
			return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("incomplete_captcha").getBytes("UTF-8")));
		}
		return sendCode(ctx, req, resourceBundle, document);
	}

	private DefaultFullHttpResponse sendCode(ChannelHandlerContext ctx, FullHttpReq req, ResourceBundle bundle, Document doc) throws Exception {
		synchronized (codes) {
			PasswordresetCode pc = codes.stream().filter(pc1 -> pc1.username.equalsIgnoreCase(doc.getString("name"))).findFirst().orElse(null);
			Map<String, String> values = new HashMap<>();
			if (doc.getString("registerConfirmationCode") != null) {
				return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(bundle.getString("register_incomplete").getBytes("UTF-8")).retain());
			}
			if (pc != null) {
				int remainingTime = new Date(DateUtils.addMinutes(new Date(pc.getExpireTime()), RESET_TIME).getTime() - new Date().getTime()).getMinutes();
				if (remainingTime == 0)
					remainingTime = 1;
				values.put("mins", String.valueOf(remainingTime));
				return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(StrSubstitutor.replace(bundle.getString("passwordreset_too_fast"), values).getBytes("UTF-8")));
			}
			String code = generateCode();
			PasswordresetMail mail = new PasswordresetMail(req.getLanguage(), doc.getString("name"), code, WebServer.getInstance().getConfig().getValue("siteurl"), "Passwordreset", new Address[]{new InternetAddress(doc.getString("email"))}, null, null);
			WebServer.getInstance().getMail().sendMail(mail);
			codes.add(new PasswordresetCode(doc.getString("name"), code, ((InetSocketAddress) ctx.channel().remoteAddress()).getHostString()));
			values.put("mins", String.valueOf(15));
			return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(StrSubstitutor.replace(bundle.getString("passwordreset_email_successfully_send"), values).getBytes("UTF-8")));

		}
	}

	private DefaultFullHttpResponse reedemCode(ChannelHandlerContext ctx, FullHttpReq req, ResourceBundle bundle, String code) throws Exception {
		synchronized (codes) {
			Map<String, String> values = new HashMap<>();
			String password = req.getPostAttributes().stream().filter(attribute -> attribute.getName().equalsIgnoreCase("password")).map((ThrowableFunction<? super Attribute, String>) Attribute::getValue).findFirst().orElse(null);
			PasswordresetCode passwordresetCode = codes.stream().filter(pc -> pc.code.equals(code)).findFirst().orElse(null);
			if (password == null)
				return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("Bad request! Did you modify the post parameters?".getBytes("UTF-8")));
			if (passwordresetCode == null || passwordresetCode.getExpireTime() < new Date().getTime() || !passwordresetCode.isValid())
				return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(bundle.getString("code_invalid").getBytes("UTF-8")));
			if (!passwordresetCode.address.equals(((InetSocketAddress) ctx.channel().remoteAddress()).getHostString())) {
				return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(bundle.getString("pwreset_wrong_address").getBytes("UTF-8")).retain());
			}
			if (password.length() < 8) {
				values.put("name", "Password");
				values.put("min", "8");
				return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(StrSubstitutor.replace(bundle.getString("textbox_too_few_chars"), values).getBytes("UTF-8")));
			}
			if (password.length() > 24) {
				values.put("name", "Password");
				values.put("max", "24");
				return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(StrSubstitutor.replace(bundle.getString("textbox_too_many_chars"), values).getBytes("UTF-8")));
			}
			if (!password.matches("[A-Za-z0-9\\-#]+")) {
				values.put("name", "Password");
				values.put("chars", "A-Z a-z 0-9 - #");
				return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(StrSubstitutor.replace(bundle.getString("textbox_invalid_chars"), values).getBytes("UTF-8")));
			}
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
		private String address;
		@Setter
		private boolean valid = true;

		public PasswordresetCode(String username, String code, String address) {
			this.username = username;
			this.code = code;
			this.address = address;
			expireTime = DateUtils.addMinutes(new Date(), 15).getTime();
		}
	}

	private String generateCode() {
		return RandomStringUtils.random(new Random().nextInt((16 - 6) + 1), "0123456789") + System.currentTimeMillis();
	}
}
