package com.github.leeonky.dal.extensions.basic.file;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.basic.file.util.PathJavaClassPropertyAccessor;
import com.github.leeonky.dal.extensions.basic.file.util.PathListAccessor;
import com.github.leeonky.dal.extensions.basic.file.util.ToString;
import com.github.leeonky.dal.extensions.basic.file.util.Util;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;

import java.io.FileInputStream;
import java.nio.file.Path;

import static com.github.leeonky.util.Suppressor.get;

@SuppressWarnings("unused")
public class PathExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        RuntimeContextBuilder runtimeContextBuilder = dal.getRuntimeContextBuilder();
        runtimeContextBuilder.registerImplicitData(Path.class, file -> get(() -> new FileInputStream(file.toFile())))
                .registerListAccessor(Path.class, new PathListAccessor())
                .registerPropertyAccessor(Path.class, new PathJavaClassPropertyAccessor())
                .registerDumper(Path.class, data -> ((Path) data.getInstance()).toFile().isDirectory()
                        ? Util.PATH_DIR_DUMPER : Util.PATH_FILE_DUMPER)
                .getConverter().addTypeConverter(Path.class, String.class, ToString::name)
        ;
    }
}
