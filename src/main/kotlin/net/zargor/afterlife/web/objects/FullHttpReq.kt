package net.zargor.afterlife.web.objects

import io.netty.handler.codec.http.DefaultFullHttpRequest
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.cookie.Cookie
import io.netty.handler.codec.http.cookie.ServerCookieDecoder
import io.netty.handler.codec.http.multipart.Attribute
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder
import net.zargor.afterlife.web.WebServer
import org.jtwig.JtwigModel
import org.jtwig.JtwigTemplate
import java.text.NumberFormat
import java.util.*

/**
 * A better version from [io.netty.handler.codec.http.FullHttpRequest]
 */
class FullHttpReq(val fullHttpRequest : FullHttpRequest, val main : WebServer, lang : String) : DefaultFullHttpRequest(fullHttpRequest.protocolVersion(), fullHttpRequest.method(), fullHttpRequest.uri(), fullHttpRequest.content()) {
    val cookies : Set<Cookie>
    val getParameters : Map<String, String>
    val postAttributes : List<Attribute>
    val group : Group?
    var language : String = lang

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
        //Language
        if (getParameters.containsKey("lang")) {
            language = getParameters["lang"]!!
        }
    }

    //Using sessions
    private fun getUsersGroup() : Group? {
        val cookie : Cookie? = cookies.firstOrNull { it.name() == "z-sID" }
        if (cookie != null) {
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

    fun renderHtml(fileName : String, extraAttributes : MutableMap<String, String> = mutableMapOf()) : ByteArray {
        val temp = JtwigTemplate.classpathTemplate("/pages/$fileName/$fileName.html")
        val model = JtwigModel.newModel()
        val resourceBundle = ResourceBundle.getBundle("pages/$fileName/$fileName", Locale(language), object : ResourceBundle.Control() {
            override fun getFallbackLocale(baseName : String?, locale : Locale?) : Locale {
                return Locale("en")
            }
        })
        resourceBundle.keySet().forEach { extraAttributes.put(it, resourceBundle.getString(it)) }
        extraAttributes.forEach { k, v -> model.with(k, v) }
        return temp.render(model).toByteArray(Charsets.UTF_8)
    }
}

fun main(args : Array<String>) {
    println(NumberFormat.getInstance().format(6000000))
}