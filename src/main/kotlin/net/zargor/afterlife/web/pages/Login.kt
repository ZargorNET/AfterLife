package net.zargor.afterlife.web.pages

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import net.zargor.afterlife.web.IWebRequest
import net.zargor.afterlife.web.PasswordEncrypt
import net.zargor.afterlife.web.WebRequest
import net.zargor.afterlife.web.objects.FullHttpReq
import org.bson.Document

/**
 * Created by Zargor on 10.07.2017.
 */
@WebRequest("/login", false, emptyArray())
class Login : IWebRequest {
    //TODO
    private val secure : Boolean = false

    override fun onRequest(ctx : ChannelHandlerContext, req : FullHttpReq) : DefaultFullHttpResponse {
        if (req.group != null) {
            val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TEMPORARY_REDIRECT, Unpooled.EMPTY_BUFFER.retain())
            res.headers().set(HttpHeaderNames.LOCATION, "/dashboard")
            return res
        }

        if (req.method() == HttpMethod.POST) {
            val resourceBundle = req.getClassResourceBundle("LoginClass")

            if (req.postAttributes.any { it.name == "name" } && req.postAttributes.any { it.name == "password" }) {
                val name = req.postAttributes.find { it.name == "name" }!!.value
                val password = req.postAttributes.find { it.name == "password" }!!.value

                if(name.isEmpty() || password.isEmpty()){
                    return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("incomplete_textboxes").toByteArray(Charsets.UTF_8)).retain())
                }
                if (!name.matches(Regex("[A-Za-z0-9\\-]+"))) {
                    val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(String.format(resourceBundle.getString("invalid_chars_username"), "A-Z a-z 0-9 -").toByteArray(Charsets.UTF_8)).retain())
                    return res
                }
                if (!password.matches(Regex("[A-Za-z0-9\\-_#]+"))) {
                    val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(String.format(resourceBundle.getString("invalid_chars_password"), "A-Z a-z 0-9 - _ #").toByteArray(Charsets.UTF_8)).retain())
                    return res
                }
                if(name.length > 24 || password.length > 24){
                    val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("too_many_chars").toByteArray(Charsets.UTF_8)).retain())
                    return res
                }
                val player = req.main.mongoDB.playerColl?.find(Document("name", name.toLowerCase()))?.first()
                if (player == null) {
                    val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("unknown_user").toByteArray(Charsets.UTF_8)).retain())
                    return res
                }
                if (player["password"] != PasswordEncrypt().encryptPassword(password)) {
                    val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(resourceBundle.getString("bad_password").toByteArray(Charsets.UTF_8)).retain())
                    return res
                }
                if (player["group"] == null) {
                    val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.copiedBuffer("Internal Server Error - 500".toByteArray(Charsets.UTF_8)).retain())
                    return res
                }
                if(player["banned"] != null){
                    val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN, Unpooled.copiedBuffer(String.format(resourceBundle.getString("banned"), player["banned"].toString()).toByteArray(Charsets.UTF_8)).retain())
                    return res
                }
                val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.EMPTY_BUFFER.retain())
                res.headers().add(HttpHeaderNames.SET_COOKIE, req.main.handler.sessionM.createCookieString(name, player["group"].toString(), secure))
                res.headers().set(HttpHeaderNames.LOCATION, "/dashboard")
                return res
            } else {
                return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("Bad POST parameters! Did you modify the parameters?".toByteArray(Charsets.UTF_8)).retain())
            }
        } else {
            return DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("Please use POST method!".toByteArray(Charsets.UTF_8)).retain())
        }
    }
}