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
 * Created by Zargor on 08.07.2017.
 */
@WebRequest(route = "/404", needToLogged = false, groupNeededRights = {})
public class NotFoundPage implements IWebRequest {

    @Override
    public DefaultFullHttpResponse onRequest(ChannelHandlerContext ctx, FullHttpReq req) throws Exception {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, Unpooled.copiedBuffer("Not found".getBytes(Charset.forName("UTF-8"))).retain());
    }
}