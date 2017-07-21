package net.zargor.afterlife.web.pages

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.cookie.Cookie
import io.netty.handler.codec.http.multipart.Attribute
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder
import net.zargor.afterlife.web.PasswordEncrypt
import net.zargor.afterlife.web.objects.GroupPermissions
import org.bson.Document

/**
 * Created by Zargor on 11.07.2017.
 */
@WebRequest("/register", GroupPermissions.NONE)
class Register : IWebRequest {
    //TODO
    private val secure : Boolean = false

    override fun onRequest(main : WebServer, ctx : ChannelHandlerContext, req : FullHttpRequest, cookies : Set<Cookie>, group : GroupPermissions, args : Map<String, String>?) : DefaultFullHttpResponse {
        if (group != GroupPermissions.NONE) {
            val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TEMPORARY_REDIRECT, Unpooled.EMPTY_BUFFER.retain())
            res.headers().set(HttpHeaderNames.LOCATION, "/dashboard")
            return res
        }
        if (req.method() == HttpMethod.POST) {
            val decoder = HttpPostRequestDecoder(req)
            if (decoder.getBodyHttpData("name") != null && decoder.getBodyHttpData("password") != null && decoder.getBodyHttpData("email") != null) {
                val name = (decoder.getBodyHttpData("name") as Attribute).value.toLowerCase()
                val password = (decoder.getBodyHttpData("password") as Attribute).value
                val email = (decoder.getBodyHttpData("email") as Attribute).value.toLowerCase()
                if(main.mongoDB.playerColl?.find(Document("name", name))?.first() != null){
                    return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("Account existiert bereits!".toByteArray(Charsets.UTF_8)).retain())
                }
                if (main.mongoDB.playerColl?.find(Document("email",email))?.first() != null) {
                    return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("Email existiert schon!".toByteArray(Charsets.UTF_8)).retain())
                }
                if(name.length > 24 || password.length > 24 || email.length > 100){
                    return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("Parameters are too long!".toByteArray(Charsets.UTF_8)).retain())
                }
                if(!(name.matches(Regex("[A-Za-z0-9\\-]+")) && password.matches(Regex("[A-Za-z0-9\\-#]+")) && email.matches(Regex("[A-Za-z0-9_\\-]+@{1}[A-Za-z0-9]+\\.{1}[A-Za-z]+")))){
                    return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("Textboxen enthalten ungültige Zeichen".toByteArray(Charsets.UTF_8)).retain())
                }
                if(!RecaptchaVerify(main).verifyRecaptcha((decoder.getBodyHttpData("recaptcha") as Attribute).value)){
                    return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("Fehlerhaftes Recaptcha!".toByteArray(Charsets.UTF_8)).retain())
                }
                main.mongoDB.playerColl?.insertOne(Document("name",name).append("password", PasswordEncrypt().encryptPassword(password)).append("email",email).append("group", GroupPermissions.NORMAL.toString()))
                val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.EMPTY_BUFFER.retain())
                res.headers().add(HttpHeaderNames.SET_COOKIE, main.handler.sessionM.createCookieString(name, GroupPermissions.NORMAL,secure))
                res.headers().set(HttpHeaderNames.LOCATION, "/dashboard")
                return res
            }
            return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("Invalid parameters!".toByteArray(Charsets.UTF_8)).retain())
        }else{
            return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("Please use POST method!".toByteArray(Charsets.UTF_8)).retain())
        }
    }
}