package com.github.leeonky.dal.extensions.basic.file;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.basic.file.util.Util;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.JavaClassPropertyAccessor;
import com.github.leeonky.dal.runtime.ListAccessor;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Set;

import static com.github.leeonky.util.BeanClass.create;
import static com.github.leeonky.util.Suppressor.get;

@SuppressWarnings("unused")
public class PathExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        RuntimeContextBuilder runtimeContextBuilder = dal.getRuntimeContextBuilder();
        extendPath(runtimeContextBuilder);
    }

    private void extendPath(RuntimeContextBuilder runtimeContextBuilder) {
        runtimeContextBuilder.registerImplicitData(Path.class, file -> get(() -> new FileInputStream(file.toFile())));
        runtimeContextBuilder.registerListAccessor(Path.class, new ListAccessor<Path>() {
            @Override
            public Iterable<?> toIterable(Path path) {
                return Util.listFile(path.toFile());
            }

            @Override
            public boolean isList(Path path) {
                return path.toFile().isDirectory();
            }
        });
        runtimeContextBuilder.registerPropertyAccessor(Path.class,
                new JavaClassPropertyAccessor<Path>(create(Path.class)) {

                    @Override
                    public Set<Object> getPropertyNames(Path path) {
                        File file = path.toFile();
                        return file.isDirectory() ? Util.listFileNames(file) : super.getPropertyNames(path);
                    }

                    @Override
                    public Object getValue(Path path, Object name) {
                        File file = path.toFile();
                        return file.isDirectory() ? Util.getSubFile(file, (String) name) : super.getValue(path, name);
                    }
                });
        runtimeContextBuilder.getConverter().addTypeConverter(Path.class, String.class, FileExtension.StaticMethods::name);

        runtimeContextBuilder.registerInspector(Path.class, data -> ((Path) data.getInstance()).toFile().isDirectory()
                ? Util.PATH_DIR_INSPECTOR : Util.PATH_FILE_INSPECTOR);
    }
}
