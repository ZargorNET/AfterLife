package net.zargor.afterlife.server.requests;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.Attribute;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import net.zargor.afterlife.server.WebServer;
import net.zargor.afterlife.server.objects.FullHttpReq;
import net.zargor.afterlife.server.permissionssystem.GroupPermissions;

@Data
public abstract class Module extends WebRequest {

	@NonNull
	private final String name;
	private final boolean disableable;

	private boolean disabled = false;

	public Module(String name, boolean disableable) {
		this.name = name;
		this.disableable = disableable;
	}

	public final void enableModule() {
		if (disabled) {
			disabled = false;
			onEnable();
			WebServer.getInstance().getMongoDB().getModuleColl().updateOne(Filters.eq("name", getName()), Updates.set("disabled", false));
		}
	}

	public final void disableModule() {
		if (this.disableable && !disabled) {
			disabled = true;
			onDisable();
			WebServer.getInstance().getMongoDB().getModuleColl().updateOne(Filters.eq("name", getName()), Updates.set("disabled", true));
		}
	}

	public void onEnable() {
		System.out.println(String.format("Enabling Module: \"%s\"", name));
	}

	public void onDisable() {
		System.out.println(String.format("Disabling Module: \"%s\"", name));
	}

	public abstract DefaultFullHttpResponse onModuleRequest(ChannelHandlerContext ctx, FullHttpReq req) throws Exception;

	@Override
	public DefaultFullHttpResponse onPermissionFailure(GroupPermissions[] neededRights, GroupPermissions[] usersRights) throws Exception {
		DefaultFullHttpResponse res;
		if (usersRights == null) {
			res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED, Unpooled.copiedBuffer("You need to be logged in!".getBytes(Charset.forName("UTF-8"))).retain());
		} else {
			res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN, Unpooled.copiedBuffer("Not enough permissions!".getBytes(Charset.forName("UTf-8"))).retain());
		}
		return res;
	}

	protected PostParameters getPostParametersAndCheckNull(FullHttpReq req, String... names) {
		List<Attribute> objs = new ArrayList<>();
		for (String name : names) {
			Optional<Attribute> opt = req.getPostAttributes().stream().filter(attribute -> attribute.getName().equals(name)).findFirst();
			if (!opt.isPresent()) {
				return new PostParameters(null, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer("Invalid post parameters!".getBytes())));
			}
			objs.add(opt.get());
		}
		PostParameters postParameters = new PostParameters(objs.toArray(new Attribute[0]), null);
		return objs.size() == 0 ? null : postParameters;
	}

	@Data
	@AllArgsConstructor
	protected class PostParameters {

		private final Attribute[] attributes;
		private final DefaultFullHttpResponse response;
	}
}
