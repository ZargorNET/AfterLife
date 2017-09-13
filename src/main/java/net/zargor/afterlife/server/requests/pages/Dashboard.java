package net.zargor.afterlife.server.requests.pages;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.util.HashMap;
import java.util.Optional;
import net.zargor.afterlife.server.objects.FullHttpReq;
import net.zargor.afterlife.server.permissionssystem.GroupPermissions;
import net.zargor.afterlife.server.permissionssystem.RequiredPermissions;
import net.zargor.afterlife.server.requests.Module;
import net.zargor.afterlife.server.requests.PageRequest;

/**
 * Created by Zargor on 09.07.2017.
 */
@RequiredPermissions(neededPermissions = {GroupPermissions.DEFAULT})
public class Dashboard extends PageRequest {

	public Dashboard() {
		super("/dashboard", null);
	}

	@Override
	public DefaultFullHttpResponse onRequest(ChannelHandlerContext ctx, FullHttpReq req, Module associatedModule) throws Exception {
		return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(this.renderHtml("dashboard", req.getLanguage(), Optional.of(new HashMap<String, String>() {{
			put("title", "DASHBOARD MAN!");
		}}))));
	}
}