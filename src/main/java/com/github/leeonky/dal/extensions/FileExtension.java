package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.JavaClassPropertyAccessor;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.leeonky.util.BeanClass.create;
import static java.util.Arrays.stream;

public class FileExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        RuntimeContextBuilder runtimeContextBuilder = dal.getRuntimeContextBuilder();
        runtimeContextBuilder.registerListAccessor(File.class, this::listFileNames);
        runtimeContextBuilder.registerPropertyAccessor(File.class,
                new JavaClassPropertyAccessor<File>(runtimeContextBuilder, create(File.class)) {

                    @Override
                    public Set<String> getPropertyNames(File file) {
                        if (file.isDirectory())
                            return listFileNames(file);
                        return super.getPropertyNames(file);
                    }

                    @Override
                    public Object getValue(File file, String name) {
                        if (file.isDirectory()) {
                            File subFile = new File(file, name);
                            if (subFile.exists())
                                return subFile;

//                            TODO checking FileGroup existing ?
                            return new FileGroup(file, name);
                        }
                        return super.getValue(file, name);
                    }
                });
        runtimeContextBuilder.getConverter().addTypeConverter(File.class, String.class, File::getName);
    }

    private LinkedHashSet<String> listFileNames(File file) {
        return stream(file.list()).sorted().collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
