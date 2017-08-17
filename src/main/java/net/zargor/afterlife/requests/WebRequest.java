package net.zargor.afterlife.requests;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import net.zargor.afterlife.permissionssystem.GroupPermissions;

import java.nio.charset.Charset;

public abstract class WebRequest {

    /**
     * Will be thrown when the user doesn't have the required permissions or just isnt logged in
     * Called from {@link net.zargor.afterlife.handlers.ClassHandler}
     *
     * @return A full {@link DefaultFullHttpResponse}
     */
    public abstract DefaultFullHttpResponse onPermissionFailure(GroupPermissions[] neededRights, GroupPermissions[] usersRights) throws Exception;

    /**
     * Called when an exception throws in {@link #onPermissionFailure(GroupPermissions[], GroupPermissions[])}
     *
     * @return A full {@link DefaultFullHttpResponse} which will be send to the user
     */
    public DefaultFullHttpResponse onException(Exception exe) {
        exe.printStackTrace();
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.copiedBuffer("A unknown internal server error happened! Please try again later :)!".getBytes(Charset.forName("UTF-8"))));
    }
}
