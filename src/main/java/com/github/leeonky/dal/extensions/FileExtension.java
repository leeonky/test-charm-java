package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.JavaClassPropertyAccessor;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static com.github.leeonky.util.BeanClass.create;
import static java.util.Arrays.asList;

public class FileExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        RuntimeContextBuilder runtimeContextBuilder = dal.getRuntimeContextBuilder();
        runtimeContextBuilder.registerListAccessor(File.class, file -> asList(file.listFiles()));
        runtimeContextBuilder.registerPropertyAccessor(File.class,
                new JavaClassPropertyAccessor<File>(runtimeContextBuilder, create(File.class)) {
                    @Override
                    public Set<String> getPropertyNames(File file) {
                        if (file.isDirectory())
                            return new HashSet<>(asList(file.list()));
                        return super.getPropertyNames(file);
                    }
                });

        runtimeContextBuilder.getConverter().addTypeConverter(File.class, String.class, File::getName);
    }
}
