package com.github.leeonky.dal.extensions.basic.file;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.basic.file.util.PathJavaClassPropertyAccessor;
import com.github.leeonky.dal.extensions.basic.file.util.ToString;
import com.github.leeonky.dal.extensions.basic.file.util.Util;
import com.github.leeonky.dal.runtime.*;
import com.github.leeonky.util.Sneaky;

import java.io.FileInputStream;
import java.nio.file.Path;

@SuppressWarnings("unused")
public class PathExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        RuntimeContextBuilder runtimeContextBuilder = dal.getRuntimeContextBuilder();
        runtimeContextBuilder.registerImplicitData(Path.class, file -> Sneaky.get(() -> new FileInputStream(file.toFile())))
                .registerDALCollectionFactory(Path.class, new DALCollectionFactory<Path, Path>() {
                    @Override
                    public boolean isList(Path path) {
                        return path.toFile().isDirectory();
                    }

                    @Override
                    public DALCollection<Path> create(Path path) {
                        return new CollectionDALCollection<>(Util.listFile(path));
                    }
                })
                .registerPropertyAccessor(Path.class, new PathJavaClassPropertyAccessor())
                .registerDumper(Path.class, data -> ((Path) data.value()).toFile().isDirectory()
                        ? Util.PATH_DIR_DUMPER : Util.PATH_FILE_DUMPER)
                .getConverter().addTypeConverter(Path.class, String.class, ToString::name)
        ;
    }
}
