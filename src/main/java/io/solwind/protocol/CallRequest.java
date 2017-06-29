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

    public CallRequest(String methodName, String clazz, Object[] args) {
        this.methodName = methodName;
        this.clazz = clazz;
        this.args = args;
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {

        if (Objects.nonNull(args)) {
            for (Object o : args) {
                if (!(o instanceof Serializable)) {
                    NotSerializableException notSerializableException = new NotSerializableException(String.format("%s", o.getClass()));
                    LOGGER.info("{}", notSerializableException);
                    throw notSerializableException;
                }
            }
        }

        out.writeObject(methodName);
        out.writeObject(clazz);
        out.writeObject(args);
    }

    private void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        methodName = (String) stream.readObject();
        clazz = (String) stream.readObject();
        args = (Object[]) stream.readObject();
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
}
