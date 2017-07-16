package io.solwind.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Objects;

/**
 * Created by theso on 6/19/2017.
 */
public class CallRequest implements Serializable {

    public static final Logger LOGGER = LoggerFactory.getLogger(CallRequest.class);

    private String methodName;
    private String clazz;
    private Object[] args;
    private Class[] argsType;
    private String token;

    public CallRequest(String methodName, String clazz, Object[] args, Class[] argsType, String token) {
        this.methodName = methodName;
        this.clazz = clazz;
        this.args = args;
        this.argsType = argsType;
        this.token = token;
    }

    public CallRequest(String methodName, String clazz, Object[] args, Class[] argsType) {
        this.methodName = methodName;
        this.clazz = clazz;
        this.args = args;
        this.argsType = argsType;
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {

        checkINputParameters();

        out.writeObject(methodName);
        out.writeObject(clazz);
        out.writeObject(args);
        out.writeObject(token);
        out.writeObject(argsType);
    }

    private void checkINputParameters() throws NotSerializableException {
        if (Objects.nonNull(args)) {
            for (Object o : args) {
                if (o != null && !(o instanceof Serializable)) {
                    NotSerializableException notSerializableException = new NotSerializableException(String.format("%s", o.getClass()));
                    LOGGER.info("{}", notSerializableException);
                    throw notSerializableException;
                }
            }
        }
    }

    private void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        methodName = (String) stream.readObject();
        clazz = (String) stream.readObject();
        args = (Object[]) stream.readObject();
        token = (String) stream.readObject();
        argsType = (Class[])stream.readObject();
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public String getClazz() {
        return clazz;
    }

    public String getToken() {
        return token;
    }

    public Class[] getArgsType() {
        return argsType;
    }
}
