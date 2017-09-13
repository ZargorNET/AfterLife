package net.zargor.afterlife.server.handlers;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.nio.charset.Charset;
import java.util.ArrayList;
import net.zargor.afterlife.server.WebServer;
import net.zargor.afterlife.server.objects.FullHttpReq;
import net.zargor.afterlife.server.permissionssystem.GroupPermissions;
import net.zargor.afterlife.server.requests.Module;
import org.bson.Document;

/**
 * Handles {@link Module}s
 * . The {@link #onRequest(ChannelHandlerContext, FullHttpReq)} method will be called from {@link NettyHttpRequestHandler}
 */
public class ModuleRequestHandler extends ClassHandler<Module> {


	public ModuleRequestHandler() {
		super(new ArrayList<>());
	}

	@Override
	public void addClass(Module module, Class<Module> moduleClass) throws IllegalAccessException, InstantiationException {
		super.addClass(module, moduleClass);
		Document doc = WebServer.getInstance().getMongoDB().getModuleColl().find(Filters.eq("name", module.getName())).first();
		if (doc == null) {
			WebServer.getInstance().getMongoDB().getModuleColl().insertOne(new Document("name", module.getName()).append("disabled", false));
			module.onEnable();
		} else {
			if (doc.getBoolean("disabled") == null) {
				WebServer.getInstance().getMongoDB().getModuleColl().updateOne(Filters.eq("name", module.getName()), Updates.set("disabled", false));
				module.onEnable();
			} else {
				if (!doc.getBoolean("disabled") || !module.isDisableable()) {
					module.onEnable();
					module.setDisabled(false);
				} else {
					module.setDisabled(true);
				}
			}
		}
	}

	@Override
	DefaultFullHttpResponse onRequest(ChannelHandlerContext ctx, FullHttpReq req) {
		Module module = getList().stream()
				.filter(module1 -> req.uri().substring(1).equalsIgnoreCase(module1.getName()))
				.findFirst().orElse(null);
		if (module == null)
			return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, Unpooled.copiedBuffer("Module wasn't found!".getBytes(Charset.forName("UTF-8"))));
		if (module.isDisabled())
			return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.LOCKED, Unpooled.copiedBuffer("Module is deactivated!".getBytes(Charset.forName("UTF-8"))).retain());
		try {
			GroupPermissions[] neededRights = permissionFailure(module, req);
			if (neededRights != null) {
				GroupPermissions[] userRights = req.getGroup() == null ? null : (GroupPermissions[]) req.getGroup().getPermissions().toArray();
				return module.onPermissionFailure(neededRights, userRights);
			} else
				return module.onModuleRequest(ctx, req);
		} catch (Exception exe) {
			return module.onException(exe);
		}
	}

	@Override
	DefaultFullHttpResponse onTooManyRequests(ChannelHandlerContext ctx, FullHttpReq req, TooManyRequestHandler.ConnectionRequestAmount cra) {
		return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TOO_MANY_REQUESTS, Unpooled.copiedBuffer("Too many requests! Try again later".getBytes(Charset.forName("UTF-8"))));
	}
}
