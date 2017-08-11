package net.zargor.afterlife.web.pages;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import net.zargor.afterlife.web.IWebRequest;
import net.zargor.afterlife.web.WebRequest;
import net.zargor.afterlife.web.WebServer;
import net.zargor.afterlife.web.objects.FullHttpReq;

import java.util.*;

@WebRequest(route = "/", needToLogged = false, groupNeededRights = {})
public class UnloggedMainPage implements IWebRequest {

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