package net.zargor.afterlife.server.util;

import io.netty.handler.codec.http.HttpMethod;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.apache.commons.io.IOUtils;

public class HttpClientRequest {

	/**
	 * Performs a new HTTP request
	 *
	 * @param requestBuilder See {@link RequestBuilder}
	 * @return See {@link RequestResponse}
	 * @throws IOException Will be thrown if the request can't be executed
	 */
	public static RequestResponse performRequest(RequestBuilder requestBuilder) throws IOException {
		URL newUrl = new URL(requestBuilder.getPath());

		HttpURLConnection connection = (HttpURLConnection) newUrl.openConnection();
		connection.setRequestMethod(requestBuilder.getMethod().name());
		requestBuilder.getHeaders().forEach(connection::setRequestProperty);
		connection.setConnectTimeout(5000);
		connection.setReadTimeout(5000);
		connection.setDoOutput(true);

		DataOutputStream out = new DataOutputStream(connection.getOutputStream());
		out.writeBytes(getParametersString(requestBuilder.getParams()));

		int status = connection.getResponseCode();
		String result = IOUtils.toString(connection.getInputStream(), "UTF-8");
		String contentType = connection.getHeaderField("Content-Type");
		connection.getInputStream().close();
		connection.disconnect();
		return new RequestResponse(status, result, contentType);
	}

	/**
	 * Makes from a map a valid HTTP string
	 * Example: "myField=Hey!&myPassword=abc"
	 *
	 * @param params The parameters
	 * @return The formed String
	 * @throws UnsupportedEncodingException Should never be thrown
	 */
	private static String getParametersString(Map<String, String> params) throws UnsupportedEncodingException {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			builder.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			builder.append("=");
			builder.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
			builder.append("&");
		}
		String result = builder.toString();
		return result.length() > 0 ? result.substring(0, result.length() - 1) : result;
	}

	/**
	 * A simple builder class for {@link #performRequest(RequestBuilder)}
	 */
	@Data
	public static class RequestBuilder {

		private final String path;
		private final HttpMethod method;

		private final Map<String, String> params = new HashMap<>();
		private final Map<String, String> headers = new HashMap<>();

		public RequestBuilder addParam(@NonNull String name, @NonNull String value) {
			this.params.put(name, value);
			return this;
		}

		public RequestBuilder addHeader(@NonNull String name, @NonNull String value) {
			this.headers.put(name, value);
			return this;
		}
	}

	/**
	 * A simplified http response.
	 * Including status, message and the returned content type
	 */
	@Data
	@AllArgsConstructor
	public static class RequestResponse {

		private int status;
		private String resultMessage;
		private String contentType;
	}
}
