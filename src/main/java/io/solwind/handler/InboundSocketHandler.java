package io.solwind.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.solwind.Functions;
import io.solwind.api.TokenSecurityHandler;
import io.solwind.exception.SecurityRuntimeException;
import io.solwind.protocol.CallRequest;
import io.solwind.protocol.CallResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by solwind on 6/14/17.
 */
public class InboundSocketHandler extends ChannelInboundHandlerAdapter {

    public static final Logger LOGGER = LoggerFactory.getLogger(InboundSocketHandler.class);

    private final Map<Class, Object> serviceTable;
    private final Map<Class, TokenSecurityHandler<Boolean>> handlerTable;

    public InboundSocketHandler(Map<Class, Object> serviceTable, Map<Class, TokenSecurityHandler<Boolean>> handlerTable) {
        this.serviceTable = serviceTable;
        this.handlerTable = handlerTable;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Functions.<CallRequest>deserialize().apply((byte[]) msg).ifPresent(obj -> {
            try {
                Class clazz = Class.forName(obj.getClazz());
                if (handlerTable.containsKey(clazz)
                        && !handlerTable.get(clazz).handle(obj.getToken()))
                    throw new SecurityRuntimeException("Wrong token!!");
                if (serviceTable.containsKey(clazz)) {
                    final Object o = serviceTable.get(clazz);
                    final Method method = o.getClass().getMethod(obj.getMethodName(),
                            obj.getArgsType());
                    CallResponse response = new CallResponse(method.invoke(o, obj.getArgs()));
                    Functions.serialize.apply(response).ifPresent(bytes ->
                    {
                        String size = Functions.humanReadableByteCount.apply(bytes.length);
                        LOGGER.info("Size of request: {}", size);
                        ctx.channel().writeAndFlush(Functions.byteConverter.apply(bytes));
                    });
                    ctx.channel().close();
                    return;
                }
            } catch (SecurityRuntimeException e) {
                throw e;
            } catch (Exception e) {
                LOGGER.info(e.getMessage(), e);
            } finally {
                ctx.channel().close();
            }
        });
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
