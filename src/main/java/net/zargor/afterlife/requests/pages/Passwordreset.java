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

public class Passwordreset extends PageRequest {

    public Passwordreset() {
        super("/passwordreset");
    }

    @Override
    public DefaultFullHttpResponse onRequest(ChannelHandlerContext ctx, FullHttpReq req) throws Exception {
        if (!req.getGetParameters().containsKey("code")) {
            DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TEMPORARY_REDIRECT, Unpooled.EMPTY_BUFFER);
            res.headers().set(HttpHeaderNames.LOCATION, "/?needEmailCode");
            return res;
        }
        net.zargor.afterlife.requests.modules.Passwordreset module = (net.zargor.afterlife.requests.modules.Passwordreset) WebServer.getInstance().getHandler().getModuleHandler().getList().stream().filter(module1 -> module1.getName().equals("passwordreset")).findFirst().orElse(null);
        net.zargor.afterlife.requests.modules.Passwordreset.PasswordresetCode code = module.getCodes().stream().filter(passwordresetCode -> passwordresetCode.getCode().equals(req.getGetParameters().get("code"))).findFirst().orElse(null);

        if (code == null) {
            DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TEMPORARY_REDIRECT, Unpooled.EMPTY_BUFFER);
            res.headers().set(HttpHeaderNames.LOCATION, "/?needEmailCode");
            return res;
        }
        if (code.getExpireTime() < new Date().getTime()) {
            DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TEMPORARY_REDIRECT, Unpooled.EMPTY_BUFFER);
            res.headers().set(HttpHeaderNames.LOCATION, "/?emailCodeExpired");
            return res;
        }
        DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TEMPORARY_REDIRECT, Unpooled.EMPTY_BUFFER);
        res.headers().set(HttpHeaderNames.LOCATION, String.format("/?emailCodeInput&code=%s", code.getCode()));
        return res;
    }
}
