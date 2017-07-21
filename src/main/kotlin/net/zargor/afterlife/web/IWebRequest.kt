package net.zargor.afterlife.web

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.DefaultFullHttpResponse
import net.zargor.afterlife.web.objects.FullHttpReq

/**
 * The interface for the web request method
 */
interface IWebRequest {
    fun onRequest(ctx : ChannelHandlerContext, req : FullHttpReq) : DefaultFullHttpResponse
}