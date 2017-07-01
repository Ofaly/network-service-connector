package io.solwind.impl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.solwind.api.RmiConnectorClient;
import io.solwind.handler.ClientChannelInboundHandlerAdapter;

/**
 * Created by theso on 6/30/2017.
 */
public class NettyIoRmiConnectorClient implements RmiConnectorClient {

    private ChannelFuture channelFuture;

    private String host;

    private Integer port;

    private ClientChannelInboundHandlerAdapter clientChannelInboundHandlerAdapter;

    public NettyIoRmiConnectorClient(String host, Integer port) throws InterruptedException {
        this.host = host;
        this.port = port;
        connect();
    }

    public NettyIoRmiConnectorClient() {
    }

    @Override
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public void setPort(Integer port) {
        this.port = port;
    }

    private void connect() throws InterruptedException {
        clientChannelInboundHandlerAdapter = new ClientChannelInboundHandlerAdapter();
        Bootstrap bootstrap = new Bootstrap().group(new NioEventLoopGroup()).channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new LoggingHandler(LogLevel.INFO))
                .handler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new ByteArrayDecoder());
                        pipeline.addLast(new ByteArrayEncoder());
                        pipeline.addLast(clientChannelInboundHandlerAdapter);
                    }
                });
        channelFuture = bootstrap.connect(host, port).sync();
    }

    @Override
    public <T> T lastResponse() {
        try {
            return (T) clientChannelInboundHandlerAdapter.getResponse();
        } finally {
            clientChannelInboundHandlerAdapter.resetResponse();
        }
    }

    @Override
    public void writeAndFlush(byte[] data) {
        channelFuture.channel().writeAndFlush(data);
    }

    @Override
    public void waitForResponse() throws InterruptedException {
        channelFuture.channel().closeFuture().sync();
    }

    @Override
    public void reconnect() throws InterruptedException {
        connect();
    }
}
