package org.testcharm.cucumber.swarm.util;

import org.testcharm.util.Sneaky;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class IoUtil {
    public static byte[] readAll(InputStream stream) {
        return Sneaky.get(() -> {
            try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                int size;
                byte[] data = new byte[1024];
                while ((size = stream.read(data, 0, data.length)) != -1)
                    buffer.write(data, 0, size);
                return buffer.toByteArray();
            }
        });
    }
}
