package net.zargor.afterlife;

import com.google.gson.Gson;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import lombok.Getter;
import net.zargor.afterlife.handlers.NettyHttpRequestHandler;
import net.zargor.afterlife.mail.MailManagement;
import net.zargor.afterlife.mongodb.MongoDB;
import net.zargor.afterlife.permissionssystem.GroupManagement;
import net.zargor.afterlife.permissionssystem.SessionManagement;

/**
 * Mainclass
 */

public class WebServer {

    @Getter
    private static WebServer instance;

    @Getter
    private Config config;
    @Getter
    private MongoDB mongoDB;
    @Getter
    private NettyHttpRequestHandler handler;
    @Getter
    private GroupManagement groupManagement;
    @Getter
    private SessionManagement sessionManagement;
    @Getter
    private MailManagement mail;
    @Getter
    private boolean epollAvailable;
    @Getter
    private ServerBootstrap bootstrap;

    private Channel channel;
    @Getter
    private Gson gson = new Gson();

    public WebServer() {
        instance = this;
        config = new Config();
        mongoDB = new MongoDB();
        handler = new NettyHttpRequestHandler();
        groupManagement = new GroupManagement();
        sessionManagement = new SessionManagement();
        mail = new MailManagement();
        epollAvailable = Epoll.isAvailable();
        bootstrap = new ServerBootstrap();


        EventLoopGroup masterGroup = epollAvailable ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        EventLoopGroup workerGroup = epollAvailable ? new EpollEventLoopGroup() : new NioEventLoopGroup();

        try {
            bootstrap.group(masterGroup, workerGroup)
                    .channel(epollAvailable ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addFirst("codec", new HttpServerCodec());
                            ch.pipeline().addAfter("codec", "aggregator", new HttpObjectAggregator(30 * 1024, true));
                            ch.pipeline().addAfter("aggregator", "handler", handler);
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);
            channel = bootstrap.bind((String) config.getValue("webserver_host"), config.getValue("webserver_port")).sync().channel();
            System.out.println("HTTP-Server started");
            channel.closeFuture().sync();

        } catch (InterruptedException exe) {
            exe.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            masterGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new WebServer();
    }
}
