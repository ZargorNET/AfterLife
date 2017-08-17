package net.zargor.afterlife.handlers;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import net.zargor.afterlife.objects.FullHttpReq;
import net.zargor.afterlife.permissionssystem.GroupPermissions;
import net.zargor.afterlife.requests.PageRequest;

import java.nio.charset.Charset;
import java.util.*;

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

        try {
            GroupPermissions[] neededRights = permissionFailure(pageRequest, req);
            if (neededRights != null) {
                GroupPermissions[] userRights = req.getGroup() == null ? null : req.getGroup().getPermissions().toArray(GroupPermissions.values());
                return pageRequest.onPermissionFailure(neededRights, userRights);
            } else {
                return pageRequest.onRequest(ctx, req);
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
