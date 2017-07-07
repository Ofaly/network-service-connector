package io.solwind.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
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
//        System.out.println("Size: " + humanReadableByteCount(byteArrayOutputStream.size(), true));
        try(ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()))) {
            return (CallResponse) stream.readObject();
        } catch (Exception e) {
            throw new DedicatedRuntimeException(e);
        } finally {
            byteArrayOutputStream = new ByteArrayOutputStream();
        }
    }

//    public static String humanReadableByteCount(long bytes, boolean si) {
//        int unit = si ? 1000 : 1024;
//        if (bytes < unit) return bytes + " B";
//        int exp = (int) (Math.log(bytes) / Math.log(unit));
//        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
//        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
//    }

}
