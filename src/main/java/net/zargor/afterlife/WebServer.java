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
import net.zargor.afterlife.passwords.PasswordEncrypt;
import net.zargor.afterlife.permissionssystem.GroupManagement;
import net.zargor.afterlife.permissionssystem.SessionManagement;

/**
 * Mainclass
 */
@Getter
public class WebServer {

    @Getter
    private static WebServer instance;

    private Config config;
    private MongoDB mongoDB;
    private NettyHttpRequestHandler handler;
    private GroupManagement groupManagement;
    private SessionManagement sessionManagement;
    private MailManagement mail;
    private ServerBootstrap bootstrap;
    private Gson gson;
    private PasswordEncrypt passwordEncrypt;

    private Channel channel;
    private boolean epollAvailable;

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
        gson = new Gson();
        passwordEncrypt = new PasswordEncrypt();

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
