package net.zargor.afterlife.web

import com.google.gson.JsonParser
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import java.util.*

/**
 * Checks if the Recaptcha is valid
 */
class RecaptchaVerify(webServer : WebServer){
    private val secret : String = webServer.config.config["grecaptcha_private_key"] as String
    fun verifyRecaptcha(recpatcha_response : String) : Boolean {
        val request = HttpPost("https://www.google.com/recaptcha/api/siteverify")
        val params = ArrayList<NameValuePair>()
        params.add(BasicNameValuePair("secret", secret))
        params.add(BasicNameValuePair("response", recpatcha_response))
        request.entity = UrlEncodedFormEntity(params)
        val httpClient = HttpClients.createDefault()
        val response = httpClient.execute(request)
        val entity = response.entity
        val entityContents = EntityUtils.toString(entity)
        val jelement = JsonParser().parse(entityContents)
        val jobject = jelement.getAsJsonObject()
        response.close()
        return jobject.get("success").asBoolean
    }
}