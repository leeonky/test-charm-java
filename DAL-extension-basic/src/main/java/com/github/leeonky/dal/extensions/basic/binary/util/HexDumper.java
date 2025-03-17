package com.github.leeonky.dal.extensions.basic.binary.util;

import com.github.leeonky.dal.runtime.Data.Resolved;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;
import com.github.leeonky.util.Sneaky;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static com.github.leeonky.util.function.Extension.getFirstPresent;
import static java.lang.String.format;

public class HexDumper implements Dumper {

    private static String dumpByteArray(byte[] data) {
        if (data.length == 0)
            return "Empty binary";
        StringBuilder builder = new StringBuilder().append("Binary size ").append(data.length);
        int lineCount = 16;
        for (int i = 0; i < data.length; i += lineCount) {
            builder.append("\n");
            append16Bytes(i, lineCount, builder, data);
        }
        return builder.toString();
    }

    private static void append16Bytes(int index, int lineCount, StringBuilder builder, byte[] data) {
        builder.append(format("%08X:", index));
        int length = Math.min(data.length - index, lineCount);
        appendBytes(index, builder, length, data);
        appendPlaceholders(lineCount, builder, length);
        builder.append(' ');
        for (int i = 0; i < length; i++)
            builder.append(toChar(data[index + i]));
    }

    private static String toChar(byte c) {
        return format("%c", Character.isValidCodePoint(c) && !Character.isISOControl(c) ? c : '.');
    }

    private static void appendPlaceholders(int lineCount, StringBuilder builder, int length) {
        for (int i = length; i < lineCount; i++) {
            if ((i & 3) == 0)
                builder.append(' ');
            builder.append("   ");
        }
    }

    private static void appendBytes(int index, StringBuilder builder, int length, byte[] data) {
        for (int i = 0; i < length; i++) {
            if ((i & 3) == 0 && i > 0)
                builder.append(' ');
            builder.append(format(" %02X", data[index + i]));
        }
    }

    public static byte[] extractBytes(Resolved obj) {
        return getFirstPresent(
                () -> obj.cast(byte[].class),
                () -> obj.cast(InputStream.class).map(stream -> Sneaky.get(() -> {
                    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                        int size;
                        byte[] buffer = new byte[1024];
                        while ((size = stream.read(buffer, 0, buffer.length)) != -1)
                            outputStream.write(buffer, 0, size);
                        return outputStream.toByteArray();
                    }
                })),
                () -> obj.cast(Byte[].class).map(bytes -> {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            for (Byte b : bytes)
                                stream.write(b);
                            return stream.toByteArray();
                        }
                )).orElseThrow(() -> new IllegalArgumentException(obj.value() + " is not binary type")); //
    }


    @Override
    public void dump(Resolved data, DumpingBuffer context) {
        context.append(dumpByteArray(extractBytes(data)));
    }
}
