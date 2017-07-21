package net.zargor.afterlife.web

import com.google.gson.Gson
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import net.zargor.afterlife.web.mongodb.Config
import net.zargor.afterlife.web.mongodb.MongoDB

/**
 * Mainclass
 */
class WebServer() {
    val config : Config = Config()
    val mongoDB : MongoDB = MongoDB(config)
    val handler = HttpHandler(this,this.javaClass.`package`.name + ".pages")
    val isEpollAvailable : Boolean = Epoll.isAvailable()
    val bootstrap : ServerBootstrap = ServerBootstrap()
    val channel : Channel
    val gson : Gson = Gson()

    init {
        val masterGroup : EventLoopGroup = if (isEpollAvailable) EpollEventLoopGroup() else NioEventLoopGroup()
        val workerGroup : EventLoopGroup = if (isEpollAvailable) EpollEventLoopGroup() else NioEventLoopGroup()

        try {
            bootstrap.group(masterGroup, workerGroup)
                    .channel(if (isEpollAvailable) EpollServerSocketChannel().javaClass else NioServerSocketChannel().javaClass)
                    .childHandler(object : ChannelInitializer<SocketChannel>() {
                        override fun initChannel(ch : SocketChannel?) {
                            println("New connection from: ${ch?.remoteAddress()?.hostString}")
                            ch?.pipeline()?.addFirst("codec", HttpServerCodec())
                            ch?.pipeline()?.addAfter("codec","aggregator", HttpObjectAggregator(512 * 4096,true))
                            ch?.pipeline()?.addAfter("aggregator", "handler", HttpRequestHandler(this@WebServer))
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true)
            channel = bootstrap.bind(config.config["webserver_host"] as String, config.config["webserver_port"] as Int).sync().channel()
            println("HTTP-Server started")
            channel.closeFuture().sync()
        } finally {
            workerGroup.shutdownGracefully()
            masterGroup.shutdownGracefully()
        }
    }
}

fun main(args : Array<String>) {
    WebServer()
}
