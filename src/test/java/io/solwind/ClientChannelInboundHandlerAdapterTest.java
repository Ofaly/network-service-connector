package io.solwind;

import io.netty.channel.ChannelHandlerContext;
import io.solwind.exception.DedicatedRuntimeException;
import io.solwind.handler.ClientChannelInboundHandlerAdapter;
import io.solwind.protocol.CallResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

/**
 * Created by theso on 6/21/2017.
 */
@RunWith(JUnit4.class)
public class ClientChannelInboundHandlerAdapterTest {

    private final ClientChannelInboundHandlerAdapter clientChannelInboundHandlerAdapter = new ClientChannelInboundHandlerAdapter();

    @Mock
    private ChannelHandlerContext channelHandlerContext;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void channelRead() throws Exception {
        clientChannelInboundHandlerAdapter.channelRead(channelHandlerContext,
                Functions.byteConverter.apply(Functions.serialize.apply(new CallResponse("testvalue")).get()));
        assertEquals("testvalue", clientChannelInboundHandlerAdapter.getResponse().getResponse());
    }

    @Test(expected = NoSuchElementException.class)
    public void channelReadWithNoSerializableDto() throws Exception {
        clientChannelInboundHandlerAdapter.channelRead(channelHandlerContext,
                Functions.byteConverter.apply(Functions.serialize.apply(new CallResponse(new NoSerializableCustomDto())).get()));
        assertEquals("testvalue", clientChannelInboundHandlerAdapter.getResponse().getResponse());
    }

    @Test(expected = DedicatedRuntimeException.class)
    public void channelReadWithNoSerializableData() throws Exception {
        clientChannelInboundHandlerAdapter.channelRead(channelHandlerContext, new byte[]{1,2,3,4});
        assertNull(clientChannelInboundHandlerAdapter.getResponse());
    }

    @Test
    public void channelReadComplete() throws Exception {
        clientChannelInboundHandlerAdapter.channelReadComplete(channelHandlerContext);
        Mockito.verify(channelHandlerContext).flush();
    }

    @Test
    public void exceptionCaught() throws Exception {
        clientChannelInboundHandlerAdapter.exceptionCaught(channelHandlerContext, new Exception("Test exception"));
        Mockito.verify(channelHandlerContext).close();
    }

    @Test
    public void channelReadWithNullData() throws Exception {
        assertNull(clientChannelInboundHandlerAdapter.getResponse());
    }

}