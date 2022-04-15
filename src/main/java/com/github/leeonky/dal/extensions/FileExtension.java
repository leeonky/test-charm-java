package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.JavaClassPropertyAccessor;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.util.Suppressor;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
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
        extendFile(runtimeContextBuilder);
        extendPath(runtimeContextBuilder);
        extendFileGroup(runtimeContextBuilder);
    }

    private void extendFileGroup(RuntimeContextBuilder runtimeContextBuilder) {
        runtimeContextBuilder.registerPropertyAccessor(FileGroup.class,
                new JavaClassPropertyAccessor<FileGroup>(runtimeContextBuilder, create(FileGroup.class)) {

                    @Override
                    public Object getValue(FileGroup fileGroup, String name) {
                        return fileGroup.getFile(name);
                    }

                    @Override
                    public Set<String> getPropertyNames(FileGroup fileGroup) {
                        return fileGroup.list();
                    }
                });
    }

    private void extendPath(RuntimeContextBuilder runtimeContextBuilder) {
        runtimeContextBuilder.registerImplicitData(Path.class, file -> Suppressor.get(() -> new FileInputStream(file.toFile())));
        runtimeContextBuilder.registerListAccessor(Path.class, path -> listFile(path.toFile()));
        runtimeContextBuilder.registerPropertyAccessor(Path.class,
                new JavaClassPropertyAccessor<Path>(runtimeContextBuilder, create(Path.class)) {

                    @Override
                    public Set<String> getPropertyNames(Path path) {
                        File file = path.toFile();
                        return file.isDirectory() ? listFileNames(file) : super.getPropertyNames(path);
                    }

                    @Override
                    public Object getValue(Path path, String name) {
                        File file = path.toFile();
                        return file.isDirectory() ? getSubFile(file, name) : super.getValue(path, name);
                    }
                });
        runtimeContextBuilder.getConverter().addTypeConverter(Path.class, String.class, StaticMethods::name);
    }

    private void extendFile(RuntimeContextBuilder runtimeContextBuilder) {
        runtimeContextBuilder.registerImplicitData(File.class, file -> Suppressor.get(() -> new FileInputStream(file)));
        runtimeContextBuilder.registerListAccessor(File.class, this::listFile);
        runtimeContextBuilder.registerPropertyAccessor(File.class,
                new JavaClassPropertyAccessor<File>(runtimeContextBuilder, create(File.class)) {

                    @Override
                    public Set<String> getPropertyNames(File file) {
                        return file.isDirectory() ? listFileNames(file) : super.getPropertyNames(file);
                    }

                    @Override
                    public Object getValue(File file, String name) {
                        return file.isDirectory() ? getSubFile(file, name) : super.getValue(file, name);
                    }
                });
        runtimeContextBuilder.getConverter().addTypeConverter(File.class, String.class, File::getName);
    }

    private Object getSubFile(File file, String name) {
        File subFile = new File(file, name);
        if (subFile.exists())
            return subFile;
        if (stream(file.list()).anyMatch(f -> f.startsWith(name + ".")))
            return new IOFileFileGroup(file, name);
        throw new IllegalArgumentException(String.format("File or File Group <%s> not found", name));
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

        public static Path path(String path) {
            return Paths.get(path);
        }

        public static String name(Path path) {
            return path.toFile().getName();
        }
    }
}
