package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.JavaClassPropertyAccessor;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;

import java.io.File;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.leeonky.util.BeanClass.create;
import static java.util.Arrays.stream;

public class FileExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        RuntimeContextBuilder runtimeContextBuilder = dal.getRuntimeContextBuilder();
        runtimeContextBuilder.registerStaticMethodExtension(StaticMethods.class);
        runtimeContextBuilder.registerListAccessor(File.class, this::listFile);
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
//                            TODO return default file when no extension static method
                            return new FileGroup(file, name);
                        }
                        return super.getValue(file, name);
                    }
                });
        runtimeContextBuilder.getConverter().addTypeConverter(File.class, String.class, File::getName);
    }

    private LinkedHashSet<String> listFileNames(File file) {
        return listFile(file).stream().map(File::getName).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private LinkedHashSet<File> listFile(File file) {
        return stream(file.listFiles()).sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static class StaticMethods {
        public static File file(String path) {
            return Paths.get(path).toFile();
        }
    }
}
