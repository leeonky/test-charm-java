package com.github.leeonky.util;

import java.util.List;
import java.util.stream.Collectors;

public class NullPointerInChainException extends RuntimeException {
    public NullPointerInChainException(List<Object> chain, int level) {
        super(makeMessage(chain, level));
    }

    private static String makeMessage(List<Object> chain, int level) {
        List<String> dotChain = chain.stream().map(o -> {
            if (o instanceof Integer)
                return "[" + o + "]";
            return "." + o;
        }).collect(Collectors.toList());
        dotChain.set(level, "<" + dotChain.get(level) + ">");
        return "Failed to read value at property chain: " + String.join("", dotChain);
    }
}
