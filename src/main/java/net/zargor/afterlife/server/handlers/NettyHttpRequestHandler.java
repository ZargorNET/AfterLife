package net.zargor.afterlife.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.zargor.afterlife.server.MimeTypes;
import net.zargor.afterlife.server.objects.FullHttpReq;
import net.zargor.afterlife.server.requests.Module;
import net.zargor.afterlife.server.requests.PageRequest;
import org.apache.commons.lang3.time.DateUtils;

/**
 * The main http handler for incoming connections. It decides if the incoming request is a {@link Module}, {@link PageRequest} or a simple file request(.txt, .mp4, .png etc.)
 */
@EqualsAndHashCode
public class NettyHttpRequestHandler implements ChannelInboundHandler {

	private TooManyRequestHandler tooManyRequestHandler = new TooManyRequestHandler();

	@Getter
	private ModuleRequestHandler moduleHandler;
	@Getter
	private PageRequestHandler pageRequestHandler;

	public NettyHttpRequestHandler() {
		try {
			moduleHandler = new ModuleRequestHandler();
			pageRequestHandler = new PageRequestHandler();
			moduleHandler.addAllClasses("net.zargor.afterlife.server.requests.modules", Module.class);
			pageRequestHandler.addAllClasses("net.zargor.afterlife.server.requests.pages", PageRequest.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {

	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {

	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {

	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {

	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof FullHttpRequest && ctx != null) {
			FullHttpRequest nettyReq = (FullHttpRequest) msg;
			String orginalLanguage = RequestUtils.getLanguage(nettyReq);
			FullHttpReq req = new FullHttpReq(nettyReq, orginalLanguage);
			String path = req.uri().contains("?") ? req.uri().split("\\?")[0] : req.uri();

			System.out.println(String.format("Neue Verbindung von %s", ((InetSocketAddress) ctx.channel().remoteAddress()).getHostString()));

			DefaultFullHttpResponse res;
			if (req.method() == HttpMethod.GET) {
				//PageRequests
				if (path.contains(".")) {
					MimeTypes type = Arrays.stream(MimeTypes.values()).filter(v -> Arrays.stream(v.getEnding()).anyMatch(path::endsWith)).findFirst().orElse(null);
					if (type == null) {
						res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_ACCEPTABLE);
						ctx.writeAndFlush(res).addListener(future -> ctx.close());
						return;
					}
					if (type == MimeTypes.HTML) {
						res = firePageRequestClassHandler(ctx, req);
					} else {
						//Files like css,jpeg,jpg,png,mp4 etc.
						byte[] bytes = RequestUtils.readResourceFile(path, this.getClass());
						if (bytes == null) {
							res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, Unpooled.EMPTY_BUFFER.retain());
							ctx.writeAndFlush(res).addListener(future -> {
								res.content().release();
								ctx.close();
							});
							return;
						}
						ByteBuf byteBuf = Unpooled.copiedBuffer(bytes).retain();
						res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, byteBuf);
						res.headers().set(HttpHeaderNames.CONTENT_TYPE, type.getMimeText());
						res.headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
						res.headers().set(HttpHeaderNames.CACHE_CONTROL, type != MimeTypes.CSS ? "max-age=604800" : "no-store, must-revalidate");
						ctx.writeAndFlush(res).addListener(future -> {
							byteBuf.release();
							ctx.close();
						});
						return;
					}
				} else {
					res = firePageRequestClassHandler(ctx, req);
				}
			} else if (req.method() == HttpMethod.POST) {
				//Modules
				TooManyRequestHandler.ConnectionRequestAmount cra = tooManyRequestHandler.tooManyRequests(ctx, HttpMethod.POST);
				if (cra != null) {
					res = moduleHandler.onTooManyRequests(ctx, req, cra);
				} else {
					res = moduleHandler.onRequest(ctx, req);
				}
				if (res == null)
					throw new NullPointerException(String.format("Response from the Module(%s) classhandler must not be null", req.uri().substring(1)));
				res.headers().set(HttpHeaderNames.CONTENT_TYPE, MimeTypes.PLAIN.getMimeText());
				res.headers().set(HttpHeaderNames.CACHE_CONTROL, "no-store, must-revalidate");
			} else {
				res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED, Unpooled.copiedBuffer("Please use GET or POST method!".getBytes(Charset.forName("UTF-8"))).retain());
			}


			if (res.refCnt() == 0)
				res.retain();
			res.headers().set(HttpHeaderNames.CONTENT_LENGTH, res.content().array().length);
			if (!req.getLanguage().equals(orginalLanguage))
				res.headers().add(HttpHeaderNames.SET_COOKIE, String.format("lang=%s; Expires=%s", req.getLanguage(), DateUtils.addYears(new Date(), 1).toGMTString()));
			ctx.writeAndFlush(res)
					.addListener(future -> {
						ctx.close();
						if (res.refCnt() != 0)
							res.release();
					});
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {

	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {

	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.copiedBuffer("Internal server error - 505. Please try again later!".getBytes(Charset.forName("UTF-8"))).retain());
		ctx.writeAndFlush(res)
				.addListener(future -> {
					ctx.close();
					res.content().release();
				});
		cause.printStackTrace();
	}

	private DefaultFullHttpResponse firePageRequestClassHandler(ChannelHandlerContext ctx, FullHttpReq req) {
		DefaultFullHttpResponse res;
		TooManyRequestHandler.ConnectionRequestAmount cra = tooManyRequestHandler.tooManyRequests(ctx, HttpMethod.GET);
		if (cra != null) {
			res = pageRequestHandler.onTooManyRequests(ctx, req, cra);
		} else {
			res = pageRequestHandler.onRequest(ctx, req);
		}
		if (res == null)
			throw new NullPointerException(String.format("Response from the PageRequest(%s) classhandler must not be null", req.uri()));
		res.headers().set(HttpHeaderNames.CONTENT_TYPE, MimeTypes.HTML.getMimeText());
		res.headers().set(HttpHeaderNames.CACHE_CONTROL, "no-store, must-revalidate");
		return res;
	}
}