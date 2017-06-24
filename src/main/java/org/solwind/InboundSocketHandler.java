package org.solwind;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by solwind on 6/14/17.
 */
class InboundSocketHandler extends ChannelInboundHandlerAdapter {

    public static final Logger LOGGER = LoggerFactory.getLogger(InboundSocketHandler.class);

    private final Map<Class, Object> serviceTable;

    public InboundSocketHandler(Map<Class, Object> serviceTable) {
        this.serviceTable = serviceTable;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Functions.<CallRequest>deserialize().apply((byte[])msg).ifPresent(obj -> {
            try {
                Class clazz = Class.forName(obj.getClazz());
                if (serviceTable.containsKey(clazz)) {
                    final Object o = serviceTable.get(clazz);
                    final Method method = o.getClass().getMethod(obj.getMethodName(),
                            convertObjectsToTypes(obj.getArgs() == null ? new Object[0] : obj.getArgs()));
                    Functions.serialize.apply(new CallResponse(method.invoke(o, obj.getArgs()))).ifPresent(bytes -> ctx.channel().writeAndFlush(Functions.byteConverter.apply(bytes)));
                    ctx.channel().close();
                    return;
                }
                ctx.channel().close();
            } catch (Exception e) {
                ctx.channel().close();
                LOGGER.info(e.getMessage(), e);
            }
        });
    }

    private Class[] convertObjectsToTypes(Object[] objects) {
        return Arrays.stream(objects).map(Object::getClass).collect(Collectors.toList()).toArray(new Class[]{});
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.info(cause.getMessage(), cause);
        ctx.close();
    }
}
