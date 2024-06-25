package com.github.leeonky.jfactory.helper;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class ListValue extends ArrayList<Object> implements FlatAble {
    @Override
    public void forEach(BiConsumer<? super String, ? super Object> action) {
        for (int i = 0; i < size(); i++)
            action.accept("[" + i + "]", get(i));
    }
}
