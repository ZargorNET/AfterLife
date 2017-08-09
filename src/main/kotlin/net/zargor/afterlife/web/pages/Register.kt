package net.zargor.afterlife.web.pages

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.multipart.Attribute
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder
import net.zargor.afterlife.web.IWebRequest
import net.zargor.afterlife.web.PasswordEncrypt
import net.zargor.afterlife.web.RecaptchaVerify
import net.zargor.afterlife.web.WebRequest
import net.zargor.afterlife.web.objects.FullHttpReq
import org.bson.Document

/**
 * Created by Zargor on 11.07.2017.
 */
@WebRequest("/register", false, emptyArray())
class Register : IWebRequest {
    //TODO
    private val secure : Boolean = false

    override fun onRequest(ctx : ChannelHandlerContext, req : FullHttpReq) : DefaultFullHttpResponse {
        if (req.group != null) {
            val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TEMPORARY_REDIRECT, Unpooled.EMPTY_BUFFER.retain())
            res.headers().set(HttpHeaderNames.LOCATION, "/dashboard")
            return res
        }
        if (req.method() == HttpMethod.POST) {
            val resourceBundle = req.getClassResourceBundle("RegisterClass")
            val decoder = HttpPostRequestDecoder(req)
            if (req.postAttributes.any { it.name == "name" } && req.postAttributes.any { it.name == "password" } && req.postAttributes.any { it.name == "email" }) {
                val name = req.postAttributes.find { it.name == "name" }!!.value.toLowerCase()
                val password = req.postAttributes.find { it.name == "password" }!!.value
                val email = req.postAttributes.find { it.name == "email" }!!.value.toLowerCase()

                if (name.isEmpty() || password.isEmpty() || email.isEmpty()) {
                    return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("incomplete_textboxes").toByteArray(Charsets.UTF_8)).retain())
                }

                if (req.main.mongoDB.playerColl?.find(Document("name", name))?.first() != null) {
                    return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("account_exists").toByteArray(Charsets.UTF_8)).retain())
                }
                if (req.main.mongoDB.playerColl?.find(Document("email", email))?.first() != null) {
                    return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("email_exists").toByteArray(Charsets.UTF_8)).retain())
                }
                if (name.length > 24 || password.length > 24 || email.length > 100) {
                    return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("Parameters are too long!".toByteArray(Charsets.UTF_8)).retain())
                }
                if (name.length < 4) {
                    return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(String.format(resourceBundle.getString("name_too_short"), 4).toByteArray(Charsets.UTF_8)).retain())
                }
                if (password.length < 8) {
                    return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(String.format(resourceBundle.getString("password_too_short"), 8).toByteArray(Charsets.UTF_8)).retain())
                }
                if (!name.matches(Regex("[A-Za-z0-9\\-]+"))) {
                    return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("name_invalid_chars").toByteArray(Charsets.UTF_8)).retain())
                }
                if (!password.matches(Regex("[A-Za-z0-9\\-#]+"))) {
                    return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("password_invalid_chars").toByteArray(Charsets.UTF_8)).retain())
                }
                if (!email.matches(Regex("[A-Za-z0-9_\\-]+@{1}[A-Za-z0-9]+\\.{1}[A-Za-z]+"))) {
                    return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("email_invalid_chars").toByteArray(Charsets.UTF_8)).retain())
                }
                if (!RecaptchaVerify(req.main).verifyRecaptcha((decoder.getBodyHttpData("recaptcha") as Attribute).value)) {
                    return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("invalid_captcha").toByteArray(Charsets.UTF_8)).retain())
                }
                req.main.mongoDB.playerColl?.insertOne(Document("name", name).append("password", PasswordEncrypt().encryptPassword(password)).append("email", email).append("group", "default"))
                val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.EMPTY_BUFFER.retain())
                res.headers().add(HttpHeaderNames.SET_COOKIE, req.main.handler.sessionM.createCookieString(name, "default", secure))
                res.headers().set(HttpHeaderNames.LOCATION, "/dashboard")
                return res
            }
            return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("Invalid parameters!".toByteArray(Charsets.UTF_8)).retain())
        } else {
            return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("Please use POST method!".toByteArray(Charsets.UTF_8)).retain())
        }
    }
}