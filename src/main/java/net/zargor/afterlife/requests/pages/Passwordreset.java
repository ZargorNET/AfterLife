package net.zargor.afterlife.requests.pages;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.util.Date;
import net.zargor.afterlife.objects.FullHttpReq;
import net.zargor.afterlife.requests.Module;
import net.zargor.afterlife.requests.PageRequest;

public class Passwordreset extends PageRequest {

	public Passwordreset() {
		super("/passwordreset", "passwordreset");
	}

	@Override
	public DefaultFullHttpResponse onRequest(ChannelHandlerContext ctx, FullHttpReq req, Module associatedModule) throws Exception {
		if (!req.getGetParameters().containsKey("code")) {
			DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TEMPORARY_REDIRECT, Unpooled.EMPTY_BUFFER);
			res.headers().set(HttpHeaderNames.LOCATION, "/?needEmailCode");
			return res;
		}
		net.zargor.afterlife.requests.modules.Passwordreset.PasswordresetCode code;
		synchronized (((net.zargor.afterlife.requests.modules.Passwordreset) associatedModule).getCodes()) {
			code = ((net.zargor.afterlife.requests.modules.Passwordreset) associatedModule).getCodes().stream().filter(passwordresetCode -> passwordresetCode.getCode().equals(req.getGetParameters().get("code"))).findFirst().orElse(null);
		}

		if (code == null || !code.isValid() || code.getExpireTime() < new Date().getTime()) {
			DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TEMPORARY_REDIRECT, Unpooled.EMPTY_BUFFER);
			res.headers().set(HttpHeaderNames.LOCATION, "/?needEmailCode");
			return res;
		}
		if (code.getExpireTime() < new Date().getTime()) {
			DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TEMPORARY_REDIRECT, Unpooled.EMPTY_BUFFER);
			res.headers().set(HttpHeaderNames.LOCATION, "/?emailCodeExpired");
			return res;
		}
		DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TEMPORARY_REDIRECT, Unpooled.EMPTY_BUFFER);
		res.headers().set(HttpHeaderNames.LOCATION, String.format("/?emailCodeInput&code=%s", code.getCode()));
		return res;
	}
}
