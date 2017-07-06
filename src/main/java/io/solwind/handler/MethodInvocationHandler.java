package io.solwind.handler;

import io.solwind.api.RmiConnectorClient;
import io.solwind.protocol.CallRequest;
import io.solwind.protocol.CallResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static io.solwind.Functions.byteConverter;
import static io.solwind.Functions.serialize;

/**
 * Created by theso on 6/18/2017.
 */
public class MethodInvocationHandler implements InvocationHandler {

    private RmiConnectorClient rmiConnectorClient;
    private String token;

    public void setRmiConnectorClient(RmiConnectorClient rmiConnectorClient) {
        this.rmiConnectorClient = rmiConnectorClient;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        rmiConnectorClient.reconnect();
        serialize.apply(new CallRequest(method.getName(), method.getDeclaringClass().getCanonicalName(), args, token))
                .ifPresent(bytes -> rmiConnectorClient.writeAndFlush(byteConverter.apply(bytes)));
        rmiConnectorClient.waitForResponse();
        CallResponse response = rmiConnectorClient.lastResponse();
        return response == null ? null : response.getResponse();
    }
}
