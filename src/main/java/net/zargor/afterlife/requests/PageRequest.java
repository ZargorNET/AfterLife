package net.zargor.afterlife.requests;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import net.zargor.afterlife.objects.FullHttpReq;
import net.zargor.afterlife.permissionssystem.GroupPermissions;

import java.nio.charset.Charset;

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
}