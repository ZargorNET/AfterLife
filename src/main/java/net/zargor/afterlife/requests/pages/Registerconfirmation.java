package net.zargor.afterlife.requests.pages;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import net.zargor.afterlife.WebServer;
import net.zargor.afterlife.objects.FullHttpReq;
import net.zargor.afterlife.requests.Module;
import net.zargor.afterlife.requests.PageRequest;
import org.bson.Document;

public class Registerconfirmation extends PageRequest {

    public Registerconfirmation() {
        super("/register", "register");
    }

    @Override
    public DefaultFullHttpResponse onRequest(ChannelHandlerContext ctx, FullHttpReq req, Module associatedModule) throws Exception {
        String code = req.getGetParameters().get("code");
        if (code == null) {
            DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TEMPORARY_REDIRECT, Unpooled.EMPTY_BUFFER);
            res.headers().set(HttpHeaderNames.LOCATION, "/?invalidRegisterCode");
            return res;
        }
        Document doc = WebServer.getInstance().getMongoDB().getPlayerColl().find(Filters.eq("registerConfirmationCode", code)).first();
        if (doc == null) {
            DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TEMPORARY_REDIRECT, Unpooled.EMPTY_BUFFER);
            res.headers().set(HttpHeaderNames.LOCATION, "/?invalidRegisterCode");
            return res;
        }
        WebServer.getInstance().getMongoDB().getPlayerColl().updateOne(Filters.eq("registerConfirmationCode", code), Updates.unset("registerConfirmationCode"));
        DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TEMPORARY_REDIRECT, Unpooled.EMPTY_BUFFER);
        res.headers().set(HttpHeaderNames.LOCATION, "/?registerComplete");
        return res;
    }
}
