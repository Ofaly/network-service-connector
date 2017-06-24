package org.solwind;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static org.solwind.Functions.byteConverter;
import static org.solwind.Functions.serialize;

/**
 * Created by theso on 6/18/2017.
 */
class MethodInvocationHandler implements InvocationHandler {

    private String discoveryConfig;

    public MethodInvocationHandler(String discoveryConfig) {
        this.discoveryConfig = discoveryConfig;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        final ClientChannelInboundHandlerAdapter clientChannelInboundHandlerAdapter = new ClientChannelInboundHandlerAdapter();

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

        String[] properties = this.discoveryConfig.split(":");
        ChannelFuture sync = bootstrap.connect(properties[0], Integer.valueOf(properties[1])).sync();

        serialize.apply(new CallRequest(method.getName(), method.getDeclaringClass().getCanonicalName(), args))
                .ifPresent(bytes -> sync.channel().writeAndFlush(byteConverter.apply(bytes)));
        sync.channel().closeFuture().sync();

        CallResponse response = clientChannelInboundHandlerAdapter.getResponse();
        return response == null ? null : response.getResponse();
    }
}
