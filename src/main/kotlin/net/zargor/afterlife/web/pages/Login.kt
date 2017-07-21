package net.zargor.afterlife.web.pages

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.cookie.Cookie
import io.netty.handler.codec.http.multipart.Attribute
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder
import net.zargor.afterlife.web.PasswordEncrypt
import net.zargor.afterlife.web.objects.GroupPermissions
import net.zargor.afterlife.web.IWebRequest
import net.zargor.afterlife.web.WebRequest
import net.zargor.afterlife.web.WebServer
import org.bson.Document

/**
 * Created by Zargor on 10.07.2017.
 */
@WebRequest("/login", GroupPermissions.NONE)
class Login : IWebRequest {
    //TODO
    private val secure : Boolean = false


    override fun onRequest(main : WebServer, ctx : ChannelHandlerContext, req : FullHttpRequest, cookies : Set<Cookie>, group : GroupPermissions, args : Map<String, String>?) : DefaultFullHttpResponse {
        if (group != GroupPermissions.NONE) {
            val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TEMPORARY_REDIRECT, Unpooled.EMPTY_BUFFER.retain())
            res.headers().set(HttpHeaderNames.LOCATION, "/dashboard")
            return res
        }
        //TODO EINGELOGGT BLEIBEN EINFÜGEN
        //TODO EINGELOGGT BLEIBEN EINFÜGEN
        //TODO EINGELOGGT BLEIBEN EINFÜGEN
        //TODO EINGELOGGT BLEIBEN EINFÜGEN//TODO EINGELOGGT BLEIBEN EINFÜGEN//TODO EINGELOGGT BLEIBEN EINFÜGEN
        //TODO EINGELOGGT BLEIBEN EINFÜGEN
        //TODO EINGELOGGT BLEIBEN EINFÜGEN
        //TODO EINGELOGGT BLEIBEN EINFÜGEN
        //TODO EINGELOGGT BLEIBEN EINFÜGEN



        if (req.method() == HttpMethod.POST) {
            val decoder = HttpPostRequestDecoder(req)
            if (decoder.getBodyHttpData("name") != null && decoder.getBodyHttpData("password") != null) {
                val name = (decoder.getBodyHttpData("name") as Attribute).value
                val password = (decoder.getBodyHttpData("password") as Attribute).value

                if(name.isEmpty() || password.isEmpty()){
                   return  DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("Textboxen sind nicht vollständig ausfefüllt!".toByteArray(Charsets.UTF_8)).retain())
                }
                if(!(name.matches(Regex("[A-Za-z0-9\\-]+")) && password.matches(Regex("[A-Za-z0-9\\-_#]+")))){
                    val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("Textboxen enthalten ungültige Zeichen".toByteArray(Charsets.UTF_8)).retain())
                    return res
                }
                if(name.length > 24 || password.length > 24){
                    val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("Zu viele Zeichen in einer Textbox!".toByteArray(Charsets.UTF_8)).retain())
                    return res
                }
                val player = main.mongoDB.playerColl?.find(Document("name", name.toLowerCase()))?.first()
                if (player == null) {
                    val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("User nicht gefunden".toByteArray(Charsets.UTF_8)).retain())
                    return res
                }
                if (player["password"] != PasswordEncrypt().encryptPassword(password)) {
                    val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("Falsches Passwort".toByteArray(Charsets.UTF_8)).retain())
                    return res
                }
                if (player["group"] == null) {
                    val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.copiedBuffer("Internal Server Error - 500".toByteArray(Charsets.UTF_8)).retain())
                    return res
                }
                if(player["banned"] != null){
                    val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN, Unpooled.copiedBuffer("Du wurdest gesperrt! Grund: ${player["banned"].toString()}. Du fühlst Dich ungerecht behandelt? Dann schreibe eine Email an: info@zargor.net . Bitte nutze dieselbe Email, mit der Du Dich angemeldet hast & den Betreff: AG.LAN Entsperrung".toByteArray(Charsets.UTF_8)).retain())
                    return res
                }
                val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.EMPTY_BUFFER.retain())
                res.headers().add(HttpHeaderNames.SET_COOKIE, main.handler.sessionM.createCookieString(name,player["group"].toString(),secure))
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