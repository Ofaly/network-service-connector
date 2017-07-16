package io.solwind;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.solwind.api.TokenSecurityHandler;
import io.solwind.exception.SecurityRuntimeException;
import io.solwind.handler.InboundSocketHandler;
import io.solwind.protocol.CallRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.Times;

import java.io.NotSerializableException;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Created by theso on 6/21/2017.
 */
@RunWith(JUnit4.class)
public class InboundSocketHandlerTest {

    @Mock
    private Map<Class, Object> services;

    @Mock
    private Map<Class, TokenSecurityHandler<Boolean>> handlers;

    private InboundSocketHandler inboundSocketHandler;

    @Mock
    private ChannelHandlerContext channelHandlerContext;

    @Mock
    private Channel channel;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        inboundSocketHandler = new InboundSocketHandler(services, handlers);
    }

    @Test
    public void channelRead() throws Exception {
        Mockito.when(channelHandlerContext.channel()).thenReturn(channel);
        Mockito.when(services.containsKey(TestService.class)).thenReturn(true);
        Mockito.when(services.get(TestService.class)).thenReturn(new TestService());
        inboundSocketHandler.channelRead(channelHandlerContext,
                Functions.byteConverter.apply(Functions.serialize.apply(new CallRequest("echoText", "io.solwind.TestService", null, new Class[0])).get()));
        Mockito.verify(channel).writeAndFlush(Mockito.any());
    }

    @Test
    public void channelReadWithCorrectToken() throws Exception {
        Mockito.when(channelHandlerContext.channel()).thenReturn(channel);
        Mockito.when(services.containsKey(TestService.class)).thenReturn(true);
        Mockito.when(handlers.containsKey(TestService.class)).thenReturn(true);
        Mockito.when(services.get(TestService.class)).thenReturn(new TestService());
        Mockito.when(handlers.get(TestService.class)).thenReturn(token -> token.equals("12345"));
        inboundSocketHandler.channelRead(channelHandlerContext,
                Functions.byteConverter.apply(Functions.serialize.apply(new CallRequest("echoText", "io.solwind.TestService", null, new Class[0], "12345")).get()));
        Mockito.verify(channel).writeAndFlush(Mockito.any());
    }

    @Test(expected = SecurityRuntimeException.class)
    public void channelReadWithWrongToken() throws Exception {
        Mockito.when(channelHandlerContext.channel()).thenReturn(channel);
        Mockito.when(services.containsKey(TestService.class)).thenReturn(true);
        Mockito.when(handlers.containsKey(TestService.class)).thenReturn(true);
        Mockito.when(services.get(TestService.class)).thenReturn(new TestService());
        Mockito.when(handlers.get(TestService.class)).thenReturn(token -> token.equals("123456"));
        inboundSocketHandler.channelRead(channelHandlerContext,
                Functions.byteConverter.apply(Functions.serialize.apply(new CallRequest("echoText", "io.solwind.TestService", null, new Class[0], "12345")).get()));
    }

    @Test
    public void channelReadWithArguments() throws Exception {
        Mockito.when(channelHandlerContext.channel()).thenReturn(channel);
        Mockito.when(services.containsKey(TestService.class)).thenReturn(true);
        Mockito.when(services.get(TestService.class)).thenReturn(new TestService());
        Object[] args = new Object[1];
        Class[] argsType = new Class[1];
        argsType[0] = String.class;
        args[0] = "test";
        inboundSocketHandler.channelRead(channelHandlerContext,
                Functions.byteConverter.apply(Functions.serialize.apply(new CallRequest("args", "io.solwind.TestService", args, argsType)).get()));
        Mockito.verify(channel).writeAndFlush(Mockito.any());
    }

    @Test(expected = NoSuchElementException.class)
    public void channelReadWithNoSerializableArguments() throws Exception {
        Mockito.when(channelHandlerContext.channel()).thenReturn(channel);
        Mockito.when(services.containsKey(TestService.class)).thenReturn(true);
        Mockito.when(services.get(TestService.class)).thenReturn(new TestService());
        Object[] args = new Object[1];
        args[0] = new NoSerializableCustomDto();
        Class[] argsType = new Class[1];
        argsType[0] = String.class;
        inboundSocketHandler.channelRead(channelHandlerContext,
                Functions.byteConverter.apply(Functions.serialize.apply(new CallRequest("args", "io.solwind.TestService", args, argsType)).get()));
        Mockito.verify(channel).writeAndFlush(Mockito.any());
    }

    @Test
    public void channelReadWithWrongClassName() throws Exception {
        Mockito.when(channelHandlerContext.channel()).thenReturn(channel);
        inboundSocketHandler.channelRead(channelHandlerContext,
                Functions.byteConverter.apply(Functions.serialize.apply(new CallRequest("echoText", "io.solwind.TestService1", new Object[0], new Class[0])).get()));
        Mockito.verify(channel, new Times(0)).writeAndFlush(Mockito.any());
        Mockito.verify(channel).close();
    }

    @Test
    public void channelReadWithWrongServiceClass() throws Exception {
        Mockito.when(channelHandlerContext.channel()).thenReturn(channel);
        Mockito.when(services.containsKey(TestService.class)).thenReturn(false);
        inboundSocketHandler.channelRead(channelHandlerContext,
                Functions.byteConverter.apply(Functions.serialize.apply(new CallRequest("echoText", "io.solwind.TestService", new Object[0], new Class[0])).get()));
        Mockito.verify(channel, new Times(0)).writeAndFlush(Mockito.any());
        Mockito.verify(channel).close();
    }

    @Test
    public void channelReadComplete() throws Exception {
        inboundSocketHandler.channelReadComplete(channelHandlerContext);
        Mockito.verify(channelHandlerContext).flush();
    }

    @Test
    public void exceptionCaught() throws Exception {
        inboundSocketHandler.exceptionCaught(channelHandlerContext, new Exception("Test exception"));
        Mockito.verify(channelHandlerContext).close();
    }

    @Test
    public void channelReadWithException() {
        Mockito.when(channelHandlerContext.channel()).thenReturn(channel);
        Mockito.when(services.containsKey(TestService.class)).thenReturn(false);
        inboundSocketHandler.channelRead(channelHandlerContext,
                Functions.byteConverter.apply(Functions.serialize.apply(new CallRequest("echoText", "io.solwind.TestService2", new Object[0], new Class[0])).get()));
    }

    @Test
    public void SerializeArgumetnTypeWithNullArgs() {
        Mockito.when(channelHandlerContext.channel()).thenReturn(channel);
        Mockito.when(services.containsKey(TestService.class)).thenReturn(true);
        inboundSocketHandler.channelRead(channelHandlerContext,
                Functions.byteConverter.apply(Functions.serialize.apply(new CallRequest("echoText", "io.solwind.TestService1", null, null)).get()));
        Mockito.verify(channel, new Times(0)).writeAndFlush(Mockito.any());

    }

    @Test
    public void SerializeArgumetnTypeWithOneOfTypeNullArgs() {
        Mockito.when(channelHandlerContext.channel()).thenReturn(channel);
        Mockito.when(services.containsKey(TestService.class)).thenReturn(true);
        Class[] classes = new Class[3];
        classes[0] = String.class;
        classes[1] = Integer.class;
        classes[2] = null;
        inboundSocketHandler.channelRead(channelHandlerContext,
                Functions.byteConverter.apply(Functions.serialize.apply(new CallRequest("listParams", "io.solwind.TestService1", null, classes)).get()));
        Mockito.verify(channel, new Times(0)).writeAndFlush(Mockito.any());
    }

    @Test
    public void SerializeArgumetnTypeWithOneOfTypeNullArgs1() {
        Mockito.when(channelHandlerContext.channel()).thenReturn(channel);
        Mockito.when(services.containsKey(TestService.class)).thenReturn(true);
        Object[] objects = new Object[3];
        objects[0] = new String();
        objects[1] = new Integer(1);
        objects[2] = null;
        inboundSocketHandler.channelRead(channelHandlerContext,
                Functions.byteConverter.apply(Functions.serialize.apply(new CallRequest("listParams", "io.solwind.TestService1", objects, null)).get()));
        Mockito.verify(channel, new Times(0)).writeAndFlush(Mockito.any());
    }

    @Test(expected = NoSuchElementException.class)
    public void SerializeArgumetnTypeWithOneOfTypeNotSerializable() {
        Mockito.when(channelHandlerContext.channel()).thenReturn(channel);
        Mockito.when(services.containsKey(TestService.class)).thenReturn(true);
        Class[] classes = new Class[1];
        Object[] objects = {new NoSerializableCustomDto()};
        classes[0] = NoSerializableCustomDto.class;
        inboundSocketHandler.channelRead(channelHandlerContext,
                Functions.byteConverter.apply(Functions.serialize.apply(new CallRequest("noSerializeDtoArgument",
                        "io.solwind.TestService1", objects, classes)).get()));
    }

}