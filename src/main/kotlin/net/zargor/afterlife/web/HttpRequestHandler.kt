package net.zargor.afterlife.web

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandler
import io.netty.handler.codec.http.FullHttpRequest

/**
 * The http handler for incoming connections
 */
class HttpRequestHandler(val webServer : WebServer) : ChannelInboundHandler {
    override fun channelInactive(ctx : ChannelHandlerContext?) {
    }

    override fun userEventTriggered(ctx : ChannelHandlerContext?, evt : Any?) {
    }

    override fun channelWritabilityChanged(ctx : ChannelHandlerContext?) {
    }

    override fun channelRead(ctx : ChannelHandlerContext?, msg : Any?) {
        if (msg is FullHttpRequest && ctx != null) {
            webServer.handler.fireRequestHandling(ctx,msg)
        }
    }

    override fun channelUnregistered(ctx : ChannelHandlerContext?) {
    }

    override fun channelActive(ctx : ChannelHandlerContext?) {
    }

    override fun channelRegistered(ctx : ChannelHandlerContext?) {
    }

    override fun channelReadComplete(ctx : ChannelHandlerContext?) {
    }

    override fun handlerAdded(ctx : ChannelHandlerContext?) {
    }

    override fun exceptionCaught(ctx : ChannelHandlerContext?, cause : Throwable?) {
    }

    override fun handlerRemoved(ctx : ChannelHandlerContext?) {
    }
}