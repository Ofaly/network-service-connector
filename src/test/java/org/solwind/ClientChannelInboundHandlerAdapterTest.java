package org.solwind;

import io.netty.channel.ChannelHandlerContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.ObjectOutputStream;

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
        Assert.assertEquals("testvalue", clientChannelInboundHandlerAdapter.getResponse().getResponse());
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

}