package net.zargor.afterlife.server.requests.modules;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import net.zargor.afterlife.server.WebServer;
import net.zargor.afterlife.server.objects.FullHttpReq;
import net.zargor.afterlife.server.requests.Module;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.commons.lang3.time.DateUtils;
import org.bson.Document;

/**
 * Created by Zargor on 10.07.2017.
 */

public class Login extends Module {

	//TODO
	private boolean secure = false;

	public Login() {
		super("login", false);
	}

	@Override
	public DefaultFullHttpResponse onModuleRequest(ChannelHandlerContext ctx, FullHttpReq req) throws Exception {
		if (req.getGroup() != null) {
			return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN, Unpooled.copiedBuffer("You're already logged in!".getBytes("UTF-8")).retain());
		}
		PostParameters params = this.getPostParametersAndCheckNull(req, "name", "password", "stayLogged");
		if (params.getResponse() != null) {
			return params.getResponse();
		}
		String name = params.getAttributes()[0].getValue();
		String password = params.getAttributes()[1].getValue();
		boolean stayLogged = Boolean.parseBoolean(params.getAttributes()[2].getValue());

		ResourceBundle resourceBundle = this.getClassResourceBundle(req.getLanguage());
		Map<String, String> values = new HashMap<>();
		if (name.isEmpty()) {
			values.put("name", "Username");
			return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(StrSubstitutor.replace(resourceBundle.getString("textbox_incomplete"), values).getBytes(Charset.forName("UTF-8"))).retain());
		}
		if (password.isEmpty()) {
			values.put("name", "Password");
			return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(StrSubstitutor.replace(resourceBundle.getString("textbox_incomplete"), values).getBytes(Charset.forName("UTF-8"))).retain());
		}
		if (!name.matches("[A-Za-z0-9\\-]+")) {
			values.put("name", "Username");
			values.put("chars", "A-Z a-z 0-9 -");
			return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(StrSubstitutor.replace(resourceBundle.getString("textbox_invalid_chars"), values).getBytes(Charset.forName("UTF-8"))).retain());
		}
		if (!password.matches("[A-Za-z0-9\\-_#]+")) {
			values.put("name", "Password");
			values.put("chars", "A-Z a-z 0-9 - #");
			return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(StrSubstitutor.replace(resourceBundle.getString("textbox_invalid_chars"), values).getBytes(Charset.forName("UTF-8"))).retain());
		}
		if (name.length() > 24) {
			values.put("name", "Username");
			values.put("max", "24");
			return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(StrSubstitutor.replace(resourceBundle.getString("textbox_too_many_chars"), values).getBytes(Charset.forName("UTF-8"))).retain());
		}
		if (password.length() > 100) {
			values.put("name", "Password");
			values.put("max", "100");
			return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(StrSubstitutor.replace(resourceBundle.getString("textbox_too_many_chars"), values).getBytes(Charset.forName("UTF-8"))).retain());
		}
		Document player = WebServer.getInstance().getMongoDB().getPlayerColl().find(new Document("name", name.toLowerCase())).first();

		if (player == null) {
			values.put("name", name);
			return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(StrSubstitutor.replace(resourceBundle.getString("unknown_user"), values).getBytes(Charset.forName("UTF-8"))).retain());
		}
		if (player.getString("registerConfirmationCode") != null) {
			return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("register_incomplete").getBytes("UTF-8")).retain());
		}
		if (!WebServer.getInstance().getPasswordEncrypt().getAlgorithm().checkPassword(password, player.getString("password"))) {
			return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("bad_password").getBytes(Charset.forName("UTF-8"))).retain());
		}
		if (player.getString("group") == null) {
			return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.copiedBuffer("Internal Server Error - 500".getBytes(Charset.forName("UTF-8"))).retain());
		}
		if (player.getString("banned") != null) {
			values.put("reason", player.getString("banned"));
			return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN, Unpooled.copiedBuffer(StrSubstitutor.replace(resourceBundle.getString("youre_banned"), values).getBytes(Charset.forName("UTF-8"))).retain());
		}
		DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.EMPTY_BUFFER.retain());
		int dayTime = stayLogged ? 14 : 1;
		res.headers().add(HttpHeaderNames.SET_COOKIE, WebServer.getInstance().getSessionManagement().createCookieString(name, DateUtils.addDays(new Date(), dayTime), player.getString("group"), secure));
		res.headers().set(HttpHeaderNames.LOCATION, "/dashboard");
		return res;
	}
}