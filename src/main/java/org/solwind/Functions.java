package org.solwind;

import java.io.*;
import java.util.Optional;
import java.util.function.Function;

/**
 * Created by theso on 6/19/2017.
 */
final class Functions {

    private Functions() {
        //Private constructor.
    }

    public static final Function<Object, Optional<Byte[]>> serialize = o -> {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            new ObjectOutputStream(out).writeObject(o);
            byte[] bytes = out.toByteArray();
            Byte[] result = new Byte[out.size()];
            int i = 0;
            for (byte b: bytes) result[i++] = b;
            return Optional.of(result);
        } catch (IOException e) {
            return Optional.empty();
        }
    };

    public static <T> Function<byte[], Optional<T>> deserialize() {
        return bytes -> {
            try {
                ByteArrayInputStream out = new ByteArrayInputStream(bytes);
                return Optional.of((T) new ObjectInputStream(out).readObject());
            } catch (Exception e) {
                return Optional.empty();
            }
        };
    }

    public static final Function<Byte[], byte[]> byteConverter = bytes -> {
        byte[] result = new byte[bytes.length];
        int i = 0;
        for (Byte b: bytes) result[i++] = b;
        return result;
    };

}
