package io.solwind.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.solwind.Functions;
import io.solwind.exception.DedicatedRuntimeException;
import io.solwind.protocol.CallResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;

/**
 * Created by theso on 6/19/2017.
 */
public class ClientChannelInboundHandlerAdapter extends ChannelInboundHandlerAdapter {

    public static final Logger LOGGER = LoggerFactory.getLogger(ClientChannelInboundHandlerAdapter.class);

    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        byteArrayOutputStream.write((byte[]) msg);
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

    public CallResponse getResponse() {
        if (byteArrayOutputStream.size() == 0) return null;
        String size = Functions.humanReadableByteCount.apply(byteArrayOutputStream.size());
        LOGGER.info("Size of response: {}", size);
        try(ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()))) {
            return (CallResponse) stream.readObject();
        } catch (Exception e) {
            throw new DedicatedRuntimeException(e);
        } finally {
            byteArrayOutputStream = new ByteArrayOutputStream();
        }
    }

}
