package net.zargor.afterlife.server.handlers;

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
import net.zargor.afterlife.server.requests.PageRequest;

/**
 * Handles {@link PageRequest}s. The {@link #onRequest(ChannelHandlerContext, FullHttpReq)} method will be called from {@link NettyHttpRequestHandler}
 */
public class PageRequestHandler extends ClassHandler<PageRequest> {

	public PageRequestHandler() {
		super(new ArrayList<>());
	}

	@Override
	DefaultFullHttpResponse onRequest(ChannelHandlerContext ctx, FullHttpReq req) {
		PageRequest pageRequest = getList().stream()
				.filter(pageRequest1 -> req.uri().equalsIgnoreCase(pageRequest1.getRoute()))
				.findFirst().orElse(null);
		if (pageRequest == null)
			//TODO
			return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, Unpooled.copiedBuffer("Page wasn't found!".getBytes(Charset.forName("UTF-8"))));
		Module module = null;
		if (pageRequest.getBelongsToModuleName() != null) {
			module = WebServer.getInstance().getHandler().getModuleHandler().getList().stream().filter(module1 -> module1.getName().equalsIgnoreCase(pageRequest.getBelongsToModuleName())).findFirst().orElse(null);
			if (module == null)
				throw new NullPointerException(String.format("Module that belongs to the PageRequest(%s) doesnt exist!", pageRequest.getRoute()));
			if (module.isDisabled())
				return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.LOCKED, Unpooled.copiedBuffer("Module is deactivated!".getBytes()));
		}
		try {
			GroupPermissions[] neededRights = permissionFailure(pageRequest, req);
			if (neededRights != null) {
				GroupPermissions[] userRights = req.getGroup() == null ? null : req.getGroup().getPermissions().toArray(GroupPermissions.values());
				return pageRequest.onPermissionFailure(neededRights, userRights);
			} else {
				return pageRequest.onRequest(ctx, req, module);
			}
		} catch (Exception exe) {
			return pageRequest.onException(exe);
		}
	}

	@Override
	DefaultFullHttpResponse onTooManyRequests(ChannelHandlerContext ctx, FullHttpReq req, TooManyRequestHandler.ConnectionRequestAmount cra) {
		//TODO
		return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TOO_MANY_REQUESTS, Unpooled.copiedBuffer("Too many requests! Try again later".getBytes(Charset.forName("UTF-8"))));
	}
}
