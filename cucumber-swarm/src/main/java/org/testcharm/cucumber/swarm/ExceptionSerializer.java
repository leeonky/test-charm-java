package org.testcharm.cucumber.swarm;

import org.testcharm.util.Sneaky;

import java.io.*;
import java.util.Base64;
import java.util.Map;

public class ExceptionSerializer {
    public static void serialize(Throwable throwable, Map<String, Object> output, String key) {
        Sneaky.run(() -> {
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                try (ObjectOutputStream outputStream = new ObjectOutputStream(stream)) {
                    outputStream.writeObject(throwable);
                }
                output.put(key, Base64.getEncoder().encodeToString(stream.toByteArray()));
            } catch (NotSerializableException ig) {
                serialize(new RemoteException(throwable), output, key);
            }
        });
    }

    public static Throwable deserialize(Map<String, Object> output, String key) {
        return Sneaky.get(() -> {
            byte[] data = Base64.getDecoder().decode((String) output.get(key));
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
                return (Throwable) ois.readObject();
            }
        });
    }
}
