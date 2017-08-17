package net.zargor.afterlife.requests.pages;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import net.zargor.afterlife.WebServer;
import net.zargor.afterlife.objects.FullHttpReq;
import net.zargor.afterlife.requests.PageRequest;

import java.util.*;

public class UnloggedMainPage extends PageRequest {

    public UnloggedMainPage() {
        super("/");
    }

    @Override
    public DefaultFullHttpResponse onRequest(ChannelHandlerContext ctx, FullHttpReq req) throws Exception {
        if (req.getGroup() != null) {
            DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.MOVED_PERMANENTLY);
            res.headers().set(HttpHeaderNames.LOCATION, "/dashboard");
            return res;
        }
        String publickey = WebServer.getInstance().getConfig().getValue("grecaptcha_public_key").toString();
        byte[] bytes = req.renderHtml("login", Optional.of(new HashMap<String, String>() {{
            put("grecaptcha_publickey", publickey != null ? publickey : "invalid_config");
        }}));
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(bytes).retain());
    }
}