package net.zargor.afterlife.requests.modules;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import net.zargor.afterlife.objects.FullHttpReq;
import net.zargor.afterlife.requests.Module;

import java.nio.charset.Charset;


public class Passwordreset extends Module {

    public Passwordreset() {
        super("passwordreset", true);
    }

    @Override
    public DefaultFullHttpResponse onModuleRequest(ChannelHandlerContext ctx, FullHttpReq req) throws Exception {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer("ABC ".getBytes(Charset.forName("UTF-8"))));
    }
}
