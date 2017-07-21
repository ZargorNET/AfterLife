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
import org.jtwig.JtwigModel
import org.jtwig.JtwigTemplate

@WebRequest("/", false, emptyArray())
class UnloggedMainPage : IWebRequest{
    override fun onRequest(ctx : ChannelHandlerContext, req : FullHttpReq) : DefaultFullHttpResponse {
        if (req.group != null) {
            val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.MOVED_PERMANENTLY)
            res.headers().set(HttpHeaderNames.LOCATION, "/dashboard")
            return res
        }
        val temp = JtwigTemplate.classpathTemplate("/src/main/resources/pages/login.html")
        val model = JtwigModel.newModel().with("mayRegister", true)
        val bytes = temp.render(model).toByteArray(Charsets.UTF_8)

        return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(bytes).retain())
    }
}