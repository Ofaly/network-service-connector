package io.solwind;

import io.solwind.handler.RegistrationServiceHolder;

import java.io.*;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by theso on 6/19/2017.
 */
public final class Functions {

    private Functions() {
        //Private constructor.
    }


    public static final Function<Set<RegistrationServiceHolder>, Function<String, RegistrationServiceHolder>> searchRshByName
            = registrationServiceHolders -> s -> {
        for (RegistrationServiceHolder holder : registrationServiceHolders) {
            if (s.equals(holder.getExposerName())) return holder;
        }
        return null;
    };

    public static final Function<Integer, String> humanReadableByteCount = bytes -> {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = ("KMGTPE").charAt(exp-1) + "i";
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    };

    public static final Function<Object, Optional<Byte[]>> serialize = o -> {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            new ObjectOutputStream(out).writeObject(o);
            byte[] bytes = out.toByteArray();
            Byte[] result = new Byte[out.size()];
            int i = 0;
            for (byte b : bytes) result[i++] = b;
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
        for (Byte b : bytes) result[i++] = b;
        return result;
    };

    public static <T> Function<Supplier<T>, Function<Consumer<T>, Runner>> asyncCall() {
        return supplier -> consumer -> new Runner(supplier, consumer);
    }

}
