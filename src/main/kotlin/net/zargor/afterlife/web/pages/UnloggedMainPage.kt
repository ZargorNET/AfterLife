package net.zargor.afterlife.web.pages

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.cookie.Cookie
import net.zargor.afterlife.web.objects.GroupPermissions
import net.zargor.afterlife.web.IWebRequest
import net.zargor.afterlife.web.WebRequest
import net.zargor.afterlife.web.WebServer
import org.jtwig.JtwigTemplate
import org.jtwig.JtwigModel



@WebRequest("/", GroupPermissions.NONE)
class UnloggedMainPage : IWebRequest{
    override fun onRequest(main : WebServer, ctx : ChannelHandlerContext, req : FullHttpRequest, cookies : Set<Cookie>, group : GroupPermissions, args : Map<String, String>?) : DefaultFullHttpResponse {
        if(group != GroupPermissions.NONE){
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