package net.zargor.afterlife.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.handler.codec.http.HttpMethod;
import java.io.IOException;
import java.util.Objects;
import net.zargor.afterlife.server.util.HttpClientRequest;

/**
 * Checks if the Recaptcha is valid
 */
public class RecaptchaVerify {

	public static boolean verifyRecaptcha(String recaptcha_response) {
		if (recaptcha_response == null || Objects.equals(recaptcha_response, ""))
			return false;
		try {
			HttpClientRequest.RequestResponse res = HttpClientRequest.performRequest(new HttpClientRequest.RequestBuilder("https://www.google.com/recaptcha/api/siteverify", HttpMethod.POST)
					.addParam("secret", WebServer.getInstance().getConfig().getValue("grecaptcha_private_key"))
					.addParam("response", recaptcha_response));
			JsonElement jelemnt = new JsonParser().parse(res.getResultMessage());
			JsonObject jobject = jelemnt.getAsJsonObject();
			return jobject.get("success").getAsBoolean();
		} catch (IOException exe) {
			exe.printStackTrace();
		}
		return false;
	}
}
