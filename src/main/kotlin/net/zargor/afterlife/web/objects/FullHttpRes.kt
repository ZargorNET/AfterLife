package net.zargor.afterlife.web.objects

import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.cookie.Cookie
import io.netty.handler.codec.http.cookie.DefaultCookie
import net.zargor.afterlife.web.MimeTypes

/**
 * A better version from [io.netty.handler.codec.http.FullHttpResponse]
 */
class FullHttpRes(var content : ByteArray){
    constructor(contentString : String) : this(contentString.toByteArray(Charsets.UTF_8))

    var status : HttpResponseStatus = HttpResponseStatus.OK
    val cookiesToSet : MutableList<DefaultCookie> = mutableListOf()
    val headers : MutableMap<String,String> = mutableMapOf(Pair(HttpHeaderNames.CONTENT_TYPE.toString(), MimeTypes.HTML.mimeText), Pair(HttpHeaderNames.CACHE_CONTROL.toString(), "no-store, must-revalidate"))


    fun toNettyHttpRes() : DefaultFullHttpResponse {
        val res = DefaultFullHttpResponse(HttpVersion.HTTP_1_1,status,Unpooled.copiedBuffer(content).retain())
        if(!cookiesToSet.isEmpty())
            cookiesToSet.forEach { res.headers().add(HttpHeaderNames.SET_COOKIE,it.toString()) }
        headers.forEach { res.headers().add(it.key,it.value) }
        return res
    }

}