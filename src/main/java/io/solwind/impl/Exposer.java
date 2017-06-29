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
import io.solwind.api.DiscoveryConfig;
import io.solwind.api.IExposer;
import io.solwind.handler.InboundSocketHandler;
import io.solwind.handler.RegistrationServiceHolder;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by solwind on 6/14/17.
 */
class Exposer implements IExposer, Runnable {

    public static final Logger LOGGER = LoggerFactory.getLogger(Exposer.class);

    private final String host;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private final DiscoveryConfig discoveryConfig;


    private Map<Class, Object> serviceTable = Collections.synchronizedMap(new HashMap<Class, Object>());

    public Exposer(String host, DiscoveryConfig discoveryConfig) throws IOException, InterruptedException {
        discoveryConfig.init();
        discoveryConfig.connect();
        this.host = host;
        this.discoveryConfig = discoveryConfig;
        final Thread thread = new Thread(this);
        thread.start();
    }

    public Exposer(DiscoveryConfig discoveryConfig) throws IOException, InterruptedException {
        discoveryConfig.init();
        discoveryConfig.connect();
        this.host = discoveryConfig.props().getProperty("expose.host");
        this.discoveryConfig = discoveryConfig;
        final Thread thread = new Thread(this);
        thread.start();
    }


    public <T> void expose(T testServiceClass, String version, String shortDescription) throws KeeperException, InterruptedException {
        serviceTable.put(testServiceClass.getClass().getInterfaces()[0], testServiceClass);
        LOGGER.info("\nExpose for {} {} ", testServiceClass, String.format("\nVersion: %s\n Description: %s\n", version, shortDescription));
        this.discoveryConfig.push(testServiceClass.getClass().getInterfaces()[0].getCanonicalName(), new RegistrationServiceHolder(host, version, shortDescription));
    }

    public void stop() throws InterruptedException {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public void run() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
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
            String[] hostSplit = this.host.split(":");
            b.bind(hostSplit.length > 1?new Integer(hostSplit[1]):80).sync().channel().closeFuture().sync();
        } catch (Exception e) {
            LOGGER.info(e.getMessage(), e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
