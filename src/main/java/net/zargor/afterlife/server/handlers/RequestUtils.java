package net.zargor.afterlife.server.handlers;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;

public class RequestUtils {

	/**
	 * Reads a resource
	 *
	 * @param path The path
	 * @return The bytes or null
	 */
	protected static byte[] readResourceFile(String path, Class clazz) throws IOException {
		InputStream in = clazz.getResourceAsStream(path);
		if (in == null)
			return null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IOUtils.copy(in, out);
		IOUtils.closeQuietly(in);
		IOUtils.closeQuietly(out);
		return out.toByteArray();
	}


	/**
	 * Finds the language from the user.
	 *
	 * @param req The request
	 * @return language in 2 chars
	 */
	protected static String getLanguage(FullHttpRequest req) {
		String langFrom = req.headers().get(HttpHeaderNames.ACCEPT_LANGUAGE);
		if (langFrom == null)
			langFrom = "en";
		else
			langFrom = langFrom.substring(0, 2);

		if (req.headers().get(HttpHeaderNames.COOKIE) == null)
			return langFrom;
		Cookie langCookie = ServerCookieDecoder.STRICT.decode(req.headers().get(HttpHeaderNames.COOKIE)).stream().filter(cookie -> cookie.name().equals("lang")).findFirst().orElse(null);
		return langCookie != null ? langCookie.value() : langFrom;
	}
}
