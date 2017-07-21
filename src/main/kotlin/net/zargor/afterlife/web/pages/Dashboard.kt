package net.zargor.afterlife.web.pages

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import io.netty.handler.codec.http.cookie.Cookie
import net.zargor.afterlife.web.objects.GroupPermissions
import net.zargor.afterlife.web.IWebRequest
import net.zargor.afterlife.web.WebRequest
import net.zargor.afterlife.web.WebServer

/**
 * Created by Zargor on 09.07.2017.
 */
@WebRequest("/dashboard", GroupPermissions.ORMAL)
class Dashboard : IWebRequest {
    override fun onRequest(main : WebServer, ctx : ChannelHandlerContext, req : FullHttpRequest, cookies : Set<Cookie>, group : GroupPermissions, args : Map<String, String>?) : DefaultFullHttpResponse {
        return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,Unpooled.copiedBuffer("Under construction".toByteArray(Charsets.UTF_8)))
    }
}