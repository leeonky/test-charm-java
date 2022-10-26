package com.github.leeonky.dal.extensions.basic.file;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.basic.FileGroup;
import com.github.leeonky.dal.extensions.basic.file.util.Util;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.JavaClassPropertyAccessor;
import com.github.leeonky.dal.runtime.ListAccessor;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static com.github.leeonky.util.BeanClass.create;
import static com.github.leeonky.util.Suppressor.get;

@SuppressWarnings("unused")
public class FileExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        RuntimeContextBuilder runtimeContextBuilder = dal.getRuntimeContextBuilder();
        runtimeContextBuilder.registerStaticMethodExtension(StaticMethods.class);
        extendFile(runtimeContextBuilder);
        extendFileGroup(runtimeContextBuilder);
    }

    private void extendFileGroup(RuntimeContextBuilder runtimeContextBuilder) {
        runtimeContextBuilder.registerPropertyAccessor(FileGroup.class,
                new JavaClassPropertyAccessor<FileGroup>(create(FileGroup.class)) {

                    @Override
                    public Object getValue(FileGroup fileGroup, Object name) {
                        return fileGroup.getFile(name);
                    }

                    @Override
                    public Set<Object> getPropertyNames(FileGroup fileGroup) {
                        return fileGroup.list();
                    }
                });
    }

    private void extendFile(RuntimeContextBuilder runtimeContextBuilder) {
        runtimeContextBuilder.registerImplicitData(File.class, file -> get(() -> new FileInputStream(file)));
        runtimeContextBuilder.registerListAccessor(File.class, new ListAccessor<File>() {
            @Override
            public Iterable<?> toIterable(File file) {
                return Util.listFile(file);
            }

            @Override
            public boolean isList(File file) {
                return file.isDirectory();
            }
        });
        runtimeContextBuilder.registerPropertyAccessor(File.class,
                new JavaClassPropertyAccessor<File>(create(File.class)) {

                    @Override
                    public Set<Object> getPropertyNames(File file) {
                        return file.isDirectory() ? Util.listFileNames(file) : super.getPropertyNames(file);
                    }

                    @Override
                    public Object getValue(File file, Object name) {
                        return file.isDirectory() ? Util.getSubFile(file, (String) name) : super.getValue(file, name);
                    }
                });
        runtimeContextBuilder.getConverter().addTypeConverter(File.class, String.class, File::getName);
        runtimeContextBuilder.registerInspector(File.class, data -> ((File) data.getInstance()).isDirectory()
                ? Util.FILE_DIR_INSPECTOR : Util.FILE_FILE_INSPECTOR);
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
