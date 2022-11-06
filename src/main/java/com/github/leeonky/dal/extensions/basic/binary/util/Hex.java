package com.github.leeonky.dal.extensions.basic.binary.util;

import com.github.leeonky.interpreter.CharStream;
import com.github.leeonky.interpreter.Notation;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.Arrays.asList;

//TODO move to dumper
public class Hex {
    private final byte[] data;

    public Hex(byte[] data) {
        this.data = data;
    }

    public static Hex hex(String hexInText) {
        return new Hex(parseBinary(hexInText));
    }

    public static byte[] parseBinary(String hexInText) {
        CharStream charStream = new CharStream(hexInText);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        while (charStream.trimBlackAndComment(asList(Notation.notation("#"))).hasContent()) {
            char c = charStream.popChar();
            if (c == ',')
                continue;
            if (charStream.hasContent())
                stream.write(parseInt(format("%c%c", c, charStream.popChar()), 16));
            else
                throw new IllegalArgumentException(String.format("incomplete byte: %c, each byte should has 2 hex numbers", c));
        }
        return stream.toByteArray();
    }

    @Override
    public boolean equals(Object another) {
        return another instanceof Hex && Arrays.equals(data, ((Hex) another).data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    @Override
    public String toString() {
        if (data.length == 0)
            return "Empty binary";
        StringBuilder builder = new StringBuilder().append("Binary size ").append(data.length);
        int lineCount = 16;
        for (int i = 0; i < data.length; i += lineCount) {
            builder.append("\n");
            append16Bytes(i, lineCount, builder);
        }
        return builder.toString();
    }

    private void append16Bytes(int index, int lineCount, StringBuilder builder) {
        builder.append(format("%08X:", index));
        int length = Math.min(data.length - index, lineCount);
        appendBytes(index, builder, length);
        appendPlaceholders(lineCount, builder, length);
        builder.append(' ');
        for (int i = 0; i < length; i++)
            builder.append(toChar(data[index + i]));
    }

    private String toChar(byte c) {
        return format("%c", Character.isValidCodePoint(c) ? c : '.');
    }

    private void appendPlaceholders(int lineCount, StringBuilder builder, int length) {
        for (int i = length; i < lineCount; i++) {
            if ((i & 3) == 0)
                builder.append(' ');
            builder.append("   ");
        }
    }

    private void appendBytes(int index, StringBuilder builder, int length) {
        for (int i = 0; i < length; i++) {
            if ((i & 3) == 0 && i > 0)
                builder.append(' ');
            builder.append(format(" %02X", data[index + i]));
        }
    }
}
