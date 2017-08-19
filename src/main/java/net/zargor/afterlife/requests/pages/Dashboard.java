package net.zargor.afterlife.requests.pages;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import net.zargor.afterlife.objects.FullHttpReq;
import net.zargor.afterlife.permissionssystem.GroupPermissions;
import net.zargor.afterlife.permissionssystem.RequiredPermissions;
import net.zargor.afterlife.requests.Module;
import net.zargor.afterlife.requests.PageRequest;

import java.nio.charset.Charset;

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
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer("Under construction".getBytes(Charset.forName("UTF-8"))).retain());
    }
}