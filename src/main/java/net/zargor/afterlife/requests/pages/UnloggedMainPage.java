package net.zargor.afterlife.requests.pages;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.util.HashMap;
import java.util.Optional;
import net.zargor.afterlife.WebServer;
import net.zargor.afterlife.objects.FullHttpReq;
import net.zargor.afterlife.requests.Module;
import net.zargor.afterlife.requests.PageRequest;

public class UnloggedMainPage extends PageRequest {

	public UnloggedMainPage() {
		super("/", null);
	}

	private String publickey = WebServer.getInstance().getConfig().getValue("grecaptcha_public_key").toString();

	@Override
	public DefaultFullHttpResponse onRequest(ChannelHandlerContext ctx, FullHttpReq req, Module associatedModule) throws Exception {
		if (req.getGroup() != null) {
			DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.MOVED_PERMANENTLY);
			res.headers().set(HttpHeaderNames.LOCATION, "/dashboard");
			return res;
		}
		byte[] bytes = this.renderHtml("login", req.getLanguage(), Optional.of(new HashMap<String, String>() {{
			put("grecaptcha_publickey", publickey != null ? publickey : "invalid_config");
		}}));
		return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(bytes).retain());
	}
}