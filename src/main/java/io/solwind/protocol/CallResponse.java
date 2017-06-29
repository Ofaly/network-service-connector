package io.solwind.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by theso on 6/19/2017.
 */
public class CallResponse implements Serializable {

    public static final Logger LOGGER = LoggerFactory.getLogger(CallResponse.class);

    private Object response;

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        if (response instanceof Serializable) {
            out.writeObject(response);
        } else {
            NotSerializableException notSerializableException = new NotSerializableException(String.format("%s", response.getClass()));
            LOGGER.info("{}", notSerializableException);
            throw notSerializableException;
        }
    }

    private void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        response = stream.readObject();
    }

    public Object getResponse() {
        return response;
    }

    public CallResponse(Object response) {
        this.response = response;
    }
}
