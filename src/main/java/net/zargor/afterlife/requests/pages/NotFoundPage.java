package net.zargor.afterlife.requests.pages;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.nio.charset.Charset;
import net.zargor.afterlife.objects.FullHttpReq;
import net.zargor.afterlife.requests.Module;
import net.zargor.afterlife.requests.PageRequest;

/**
 * Created by Zargor on 08.07.2017.
 */
public class NotFoundPage extends PageRequest {

	public NotFoundPage() {
		super("/404", null);
	}

	@Override
	public DefaultFullHttpResponse onRequest(ChannelHandlerContext ctx, FullHttpReq req, Module associatedModule) throws Exception {
		return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, Unpooled.copiedBuffer("Not found".getBytes(Charset.forName("UTF-8"))).retain());
	}
}