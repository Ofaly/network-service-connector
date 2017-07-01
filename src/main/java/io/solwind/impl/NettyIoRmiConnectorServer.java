package io.solwind.impl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.solwind.api.RmiConnectorServer;
import io.solwind.handler.InboundSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by theso on 7/1/2017.
 */
public class NettyIoRmiConnectorServer implements RmiConnectorServer {

    private ServerBootstrap b;

    public static final Logger LOGGER = LoggerFactory.getLogger(NettyIoRmiConnectorServer.class);

    private Map<Class, Object> serviceTable;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private int port;

    public void serviceTable(Map<Class, Object> serviceTable) {
        this.serviceTable = serviceTable;
    }

    @Override
    public void port(int port) {
        this.port = port;
    }

    @Override
    public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    @Override
    public void run() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        b = new ServerBootstrap();
        try {
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new ByteArrayDecoder());
                            p.addLast(new ByteArrayEncoder());
                            p.addLast(new InboundSocketHandler(serviceTable));
                        }
                    });
            b.bind(port).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.info(e.getMessage(), e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
