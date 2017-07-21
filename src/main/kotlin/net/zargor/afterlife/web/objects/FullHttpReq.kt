package net.zargor.afterlife.web.objects

import io.netty.handler.codec.http.DefaultFullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.cookie.Cookie
import io.netty.handler.codec.http.cookie.ServerCookieDecoder
import io.netty.handler.codec.http.multipart.Attribute
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder
import net.zargor.afterlife.web.WebServer

/**
 * A better version from [io.netty.handler.codec.http.FullHttpRequest]
 */
class FullHttpReq(val fullHttpRequest : DefaultFullHttpRequest, val main : WebServer) : DefaultFullHttpRequest(fullHttpRequest.protocolVersion(),fullHttpRequest.method(),fullHttpRequest.uri(),fullHttpRequest.content()) {
    val cookies : Set<Cookie>
    val getParameters : Map<String,String>
    val postAttributes : List<Attribute>
    val group : Group?
    init {
        //COOKIES
        val cookiesReq = fullHttpRequest.headers().get(HttpHeaderNames.COOKIE)
        cookies = if (cookiesReq != null) ServerCookieDecoder.STRICT.decode(cookiesReq) else setOf()

        //GET_PARAMETERS
        getParameters = splitGetArguments() ?: mapOf()

        //POST_ATTRIBUTES
        postAttributes = getPostAttri()

        //GROUP
        group = getUsersGroup()
    }

    //Using sessions
    private fun getUsersGroup() : Group? {
        val cookie : Cookie? = cookies.firstOrNull { it.name() == "z-sID" }
        if(cookie != null) {
            val sID = main.handler.sessionM.getSessionsByID(cookie.value())
            return sID?.group
        }
        return null
    }

    private fun getPostAttri() : List<Attribute> {
        val attributes : MutableList<Attribute> = mutableListOf()
        val decoder = HttpPostRequestDecoder(fullHttpRequest)
        decoder.bodyHttpDatas.filter { it is Attribute }.forEach { attributes.add(it as Attribute) }
        return attributes.toList()
    }


    private fun splitGetArguments() : Map<String, String>? {
        if (uri().contains('?')) {
            val map : MutableMap<String, String> = mutableMapOf()
            val splitA = uri().split('?')
            var splitT : String = splitA[1]
            if (splitA.size > 1) {
                var index = 0
                splitA.forEach {
                    if (index > 1) {
                        splitT += "?$it"
                    }
                    index++
                }
            }
            val args : MutableList<String> = mutableListOf()
            if (splitT.contains('&')) {
                splitT.split('&').forEach { args.add(it) }
            } else {
                args.add(splitT)
            }
            args.forEach {
                if (it != "") {
                    if (it.contains('=')) {
                        val a = it.split('=')
                        map.put(a[0], a[1])
                    } else {
                        map.put(it, "true")
                    }
                }
            }
            return map
        }
        return null
    }


}