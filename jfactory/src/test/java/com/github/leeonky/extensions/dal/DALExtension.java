package com.github.leeonky.extensions.dal;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;

import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public class DALExtension implements Extension {
    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerStaticMethodExtension(DALExtension.class);
    }

    public static List<List<String>> table(String table) {
        return stream(table.split("\n")).map(line ->
                        stream(line.split("\\|")).map(String::trim).collect(toList()))
                .collect(toList());
    }
}
