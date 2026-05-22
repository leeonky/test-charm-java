package org.testcharm.cucumber.swarm;

import org.testcharm.util.Sneaky;

import java.io.*;
import java.util.Base64;
import java.util.Map;
import java.util.function.Function;

public class ExceptionSerializer {
    public static void serialize(Throwable throwable, Map<String, Object> output, String key) {
        output.put(key, serializeError(throwable));
    }

    public static String serializeError(Throwable throwable) {
        return Sneaky.get(() -> {
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                try (ObjectOutputStream outputStream = new ObjectOutputStream(stream)) {
                    outputStream.writeObject(throwable);
                }
                return Base64.getEncoder().encodeToString(stream.toByteArray());
            } catch (NotSerializableException ig) {
                return serializeError(new RemoteException(throwable));
            }
        });
    }

    public static Throwable deserialize(Map<String, Object> output, String key) {
        return toThrowable().apply((String) output.get(key));
    }

    public static Function<String, Throwable> toThrowable() {
        return Sneaky.sneakyGet(s -> {
            byte[] data = Base64.getDecoder().decode(s);
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
                return (Throwable) ois.readObject();
            }
        });
    }
}
