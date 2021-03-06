package net.zargor.afterlife.server.requests.pages;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import net.zargor.afterlife.server.objects.FullHttpReq;
import net.zargor.afterlife.server.requests.Module;
import net.zargor.afterlife.server.requests.PageRequest;

public class Test extends PageRequest {

	public Test() {
		super("/test", null);
	}

	@Override
	public DefaultFullHttpResponse onRequest(ChannelHandlerContext ctx, FullHttpReq req, Module associatedModule) throws Exception {
		return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.EMPTY_BUFFER);
	}
}
