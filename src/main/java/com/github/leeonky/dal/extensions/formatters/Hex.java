package com.github.leeonky.dal.extensions.formatters;

import com.github.leeonky.interpreter.CharStream;
import com.github.leeonky.interpreter.Notation;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.Arrays.asList;

public class Hex {
    private final byte[] data;

    public Hex(byte[] data) {
        this.data = data;
    }

    public static Hex hex(String hexInText) {
        return new Hex(parseBinary(hexInText));
    }

    private static byte[] parseBinary(String hexInText) {
        CharStream charStream = new CharStream(hexInText);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        for (; ; ) {
//            TODO need return charStream
            charStream.trimBlackAndComment(asList(Notation.notation("#")));
            if (!charStream.hasContent()) break;
//            TODO need return charStream
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
}
