package com.github.leeonky.dal.extensions.basic.file;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.basic.FileGroup;
import com.github.leeonky.dal.extensions.basic.file.util.*;
import com.github.leeonky.dal.runtime.Extension;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.leeonky.util.Suppressor.get;

@SuppressWarnings("unused")
public class FileExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerStaticMethodExtension(ToString.class)
                .registerStaticMethodExtension(StaticMethods.class)
                .registerImplicitData(File.class, file -> get(() -> new FileInputStream(file)))
                .registerListAccessor(File.class, new FileListAccessor())
                .registerPropertyAccessor(File.class, new FileJavaClassPropertyAccessor())
                .registerPropertyAccessor(FileGroup.class, new FileGroupJavaClassPropertyAccessor())
                .registerDumper(File.class, data -> ((File) data.getInstance()).isDirectory()
                        ? Util.FILE_DIR_DUMPER : Util.FILE_FILE_DUMPER);
        dal.getRuntimeContextBuilder().getConverter()
                .addTypeConverter(File.class, String.class, File::getName);
    }

    //    TODO mv to string
    public static class StaticMethods {
        public static File file(String path) {
            return Paths.get(path).toFile();
        }

        public static Path path(String path) {
            return Paths.get(path);
        }
    }
}
