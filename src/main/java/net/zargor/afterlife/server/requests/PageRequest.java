package net.zargor.afterlife.server.requests;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import net.zargor.afterlife.server.objects.FullHttpReq;
import net.zargor.afterlife.server.permissionssystem.GroupPermissions;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

@Data
@AllArgsConstructor
public abstract class PageRequest extends WebRequest {

	@NonNull
	private final String route;
	private String belongsToModuleName;

	public abstract DefaultFullHttpResponse onRequest(ChannelHandlerContext ctx, FullHttpReq req, Module associatedModule) throws Exception;

	@Override
	public DefaultFullHttpResponse onPermissionFailure(GroupPermissions[] neededRights, GroupPermissions[] usersRights) throws Exception {
		//TODO
		DefaultFullHttpResponse res;
		if (usersRights == null) {
			res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TEMPORARY_REDIRECT, Unpooled.copiedBuffer("You need to be logged in!".getBytes(Charset.forName("UTF-8"))).retain());
			res.headers().set(HttpHeaderNames.LOCATION, "/?needLogin");
		} else {
			res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN, Unpooled.copiedBuffer("Not enough permissions!".getBytes(Charset.forName("UTf-8"))).retain());
		}
		return res;
	}

	public byte[] renderHtml(String fileName, String lang, Optional<Map<String, String>> optionalExtraAttributes) {
		JtwigTemplate temp = JtwigTemplate.classpathTemplate(String.format("/pages/%s.html", fileName));
		JtwigModel model = JtwigModel.newModel();
		ResourceBundle resourceBundle = ResourceBundle.getBundle("translations/frontend/frontend", new Locale(lang), new ResourceBundle.Control() {
			@Override
			public Locale getFallbackLocale(String baseName, Locale locale) {
				return new Locale("en");
			}
		});
		Map<String, String> extraAttributes;
		extraAttributes = optionalExtraAttributes.orElseGet(HashMap::new);
		resourceBundle.keySet().forEach(s -> extraAttributes.put(s, resourceBundle.getString(s)));
		extraAttributes.forEach(model::with);

		return temp.render(model).getBytes(Charset.forName("UTF-8"));
	}
}