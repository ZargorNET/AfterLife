package net.zargor.afterlife.web.pages;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import net.zargor.afterlife.web.IWebRequest;
import net.zargor.afterlife.web.WebRequest;
import net.zargor.afterlife.web.objects.FullHttpReq;

import java.nio.charset.Charset;

/**
 * Created by Zargor on 09.07.2017.
 */
@WebRequest(route = "/dashboard", needToLogged = true, groupNeededRights = {})
public class Dashboard implements IWebRequest {

    @Override
    public DefaultFullHttpResponse onRequest(ChannelHandlerContext ctx, FullHttpReq req) throws Exception {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer("Under construction".getBytes(Charset.forName("UTF-8"))).retain());
    }
}