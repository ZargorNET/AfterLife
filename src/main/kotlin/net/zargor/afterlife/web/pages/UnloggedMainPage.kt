package net.zargor.afterlife.web.pages

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import net.zargor.afterlife.web.IWebRequest
import net.zargor.afterlife.web.WebRequest
import net.zargor.afterlife.web.objects.FullHttpReq

@WebRequest("/", false, emptyArray())
class UnloggedMainPage : IWebRequest{
    override fun onRequest(ctx : ChannelHandlerContext, req : FullHttpReq) : DefaultFullHttpResponse {
        if (req.group != null) {
            val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.MOVED_PERMANENTLY)
            res.headers().set(HttpHeaderNames.LOCATION, "/dashboard")
            return res
        }
        val bytes = req.renderHtml("login", mutableMapOf(Pair("grecaptcha_publickey", "${req.main.config.config["grecaptcha_public_key"] ?: "invalid_config"}")))

        return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(bytes).retain())
    }
}