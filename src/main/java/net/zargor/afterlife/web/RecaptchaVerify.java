package net.zargor.afterlife.web;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.util.*;

/**
 * Checks if the Recaptcha is valid
 */
public class RecaptchaVerify {

    private String secret = WebServer.getInstance().getConfig().getValue("grecaptcha_private_key").toString();

    public boolean verifyRecaptcha(String recaptcha_response) {
        if (recaptcha_response == null)
            return false;
        try {
            HttpPost request = new HttpPost("https://www.google.com/recaptcha/api/siteverify");
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("secret", secret));
            params.add(new BasicNameValuePair("response", recaptcha_response));
            request.setEntity(new UrlEncodedFormEntity(params));
            HttpClient httpClient = HttpClients.createDefault();
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            String entityContents = EntityUtils.toString(entity);
            JsonElement jelemnt = new JsonParser().parse(entityContents);
            JsonObject jobject = jelemnt.getAsJsonObject();
            return jobject.get("success").getAsBoolean();
        } catch (IOException exe) {
            exe.printStackTrace();
        }
        return false;
    }
}
