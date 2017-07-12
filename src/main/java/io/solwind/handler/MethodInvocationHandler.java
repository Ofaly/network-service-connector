package io.solwind.handler;

import io.solwind.api.RmiConnectorClient;
import io.solwind.protocol.CallRequest;
import io.solwind.protocol.CallResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static io.solwind.Functions.byteConverter;
import static io.solwind.Functions.serialize;

/**
 * Created by theso on 6/18/2017.
 */
public class MethodInvocationHandler implements InvocationHandler, Consumer<Set<RegistrationServiceHolder>> {

    private RmiConnectorClient rmiConnectorClient;
    private String token;
    private String exposerName;

    public void setRmiConnectorClient(RmiConnectorClient rmiConnectorClient) {
        this.rmiConnectorClient = rmiConnectorClient;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setExposerName(String exposerName) {
        this.exposerName = exposerName;
    }

    public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        serialize.apply(new CallRequest(method.getName(), method.getDeclaringClass().getCanonicalName(), args, token))
                .ifPresent(bytes -> rmiConnectorClient.writeAndFlush(byteConverter.apply(bytes)));
        rmiConnectorClient.waitForResponse();
        CallResponse response = rmiConnectorClient.lastResponse();
        return response == null ? null : response.getResponse();
    }

    @Override
    public void accept(Set<RegistrationServiceHolder> o) {
        o.forEach(registrationServiceHolder -> {
            if (registrationServiceHolder.getExposerName().equals(exposerName)) {
                String[] host = registrationServiceHolder.getHost().split(":");
                rmiConnectorClient.setHost(host[0]);
                rmiConnectorClient.setPort(new Integer(host[1]));
            }
        });
    }
}
