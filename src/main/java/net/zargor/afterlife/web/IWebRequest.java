package net.zargor.afterlife.web;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import net.zargor.afterlife.web.objects.FullHttpReq;

/**
 * The interface for the web request method
 */
public interface IWebRequest {

    DefaultFullHttpResponse onRequest(ChannelHandlerContext ctx, FullHttpReq req) throws Exception;
}