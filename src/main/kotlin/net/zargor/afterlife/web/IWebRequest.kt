package net.zargor.afterlife.web

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.cookie.Cookie
import net.zargor.afterlife.web.objects.FullHttpReq
import net.zargor.afterlife.web.objects.FullHttpRes
import net.zargor.afterlife.web.objects.GroupPermissions

/**
 * The interface for the web request method
 */
interface IWebRequest {
    fun onRequest(ctx : ChannelHandlerContext, req : FullHttpReq) : FullHttpRes
}