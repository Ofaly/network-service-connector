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
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

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
                            convertObjectsToTypes(obj.getArgs() == null ? new Object[0] : obj.getArgs()));
                    CallResponse response = new CallResponse(method.invoke(o, obj.getArgs()));
//                    System.out.println("Serialize: " + humanReadableByteCount(Functions.serialize.apply(response).create().length, true));
                    Functions.serialize.apply(response).ifPresent(bytes -> ctx.channel().writeAndFlush(Functions.byteConverter.apply(bytes)));
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

//    public static String humanReadableByteCount(long bytes, boolean si) {
//        int unit = si ? 1000 : 1024;
//        if (bytes < unit) return bytes + " B";
//        int exp = (int) (Math.log(bytes) / Math.log(unit));
//        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
//        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
//    }

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
