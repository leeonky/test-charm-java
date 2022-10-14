package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.*;
import com.github.leeonky.dal.runtime.inspector.Inspector;
import com.github.leeonky.dal.runtime.inspector.InspectorCache;
import com.github.leeonky.dal.runtime.inspector.MapInspector;
import com.github.leeonky.dal.runtime.inspector.TypeValueInspector;
import com.github.leeonky.dal.util.TextUtil;
import com.github.leeonky.util.InvocationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.leeonky.dal.extensions.SFtp.formatFileSize;
import static com.github.leeonky.util.BeanClass.create;
import static com.github.leeonky.util.Suppressor.get;
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

    private void extendPath(RuntimeContextBuilder runtimeContextBuilder) {
        runtimeContextBuilder.registerImplicitData(Path.class, file -> get(() -> new FileInputStream(file.toFile())));
        runtimeContextBuilder.registerListAccessor(Path.class, new ListAccessor<Path>() {
            @Override
            public Iterable<?> toIterable(Path path) {
                return listFile(path.toFile());
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
                        return file.isDirectory() ? listFileNames(file) : super.getPropertyNames(path);
                    }

                    @Override
                    public Object getValue(Path path, Object name) {
                        File file = path.toFile();
                        return file.isDirectory() ? getSubFile(file, (String) name) : super.getValue(path, name);
                    }
                });
        runtimeContextBuilder.getConverter().addTypeConverter(Path.class, String.class, StaticMethods::name);

        runtimeContextBuilder.registerInspector(Path.class, data -> {
            Path filePath = (Path) data.getInstance();
            if (filePath.toFile().isDirectory()) {
//            TODO refactor
                return (path, inspectorCache) -> "java.nio.Path "
                        + new MapInspector(data).inspect(path, inspectorCache).replaceFirst("^sun.nio.fs.UnixPath ", "");
            }

            return new TypeValueInspector() {
                @Override
                public String inspectType() {
                    return "java.nio.Path";
                }

                @Override
                public String inspectValue() {
                    return attribute(filePath);
                }
            };
        });
    }

    private void extendFile(RuntimeContextBuilder runtimeContextBuilder) {
        runtimeContextBuilder.registerImplicitData(File.class, file -> get(() -> new FileInputStream(file)));
        runtimeContextBuilder.registerListAccessor(File.class, new ListAccessor<File>() {
            @Override
            public Iterable<?> toIterable(File file) {
                return listFile(file);
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
                        return file.isDirectory() ? listFileNames(file) : super.getPropertyNames(file);
                    }

                    @Override
                    public Object getValue(File file, Object name) {
                        return file.isDirectory() ? getSubFile(file, (String) name) : super.getValue(file, name);
                    }
                });
        runtimeContextBuilder.getConverter().addTypeConverter(File.class, String.class, File::getName);
        runtimeContextBuilder.registerInspector(File.class, data -> {
            File file = (File) data.getInstance();
            return file.isDirectory() ? new DirInspector(data, file) : (path, cache) -> attribute(file.toPath());
        });
    }

    private String attribute(Path path) {
        PosixFileAttributes posixFileAttributes = get(() -> Files.readAttributes(path, PosixFileAttributes.class));
        return String.format("%s %s %s %4s %s %s", PosixFilePermissions.toString(posixFileAttributes.permissions()),
                posixFileAttributes.group(), posixFileAttributes.owner(), formatFileSize(path.toFile().length()),
                posixFileAttributes.lastModifiedTime(), path.getFileName().toString());
    }

    private Object getSubFile(File file, String name) {
        File subFile = new File(file, name);
        if (subFile.exists())
            return subFile;
        if (stream(file.list()).anyMatch(f -> f.startsWith(name + ".")))
            return new IOFileFileGroup(file, name);
        throw new InvocationException(new FileNotFoundException(String.format("File or File Group <%s> not found", name)));
    }

    private Set<Object> listFileNames(File file) {
        return listFile(file).stream().map(File::getName).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<File> listFile(File file) {
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

    private static class DirInspector implements Inspector {
        private final Data data;
        private final File file;

        public DirInspector(Data data, File file) {
            this.data = data;
            this.file = file;
        }

        @Override
        public String inspect(String path, InspectorCache inspectorCache) {
            return String.join("\n", new ArrayList<String>() {{
                add("java.io.File dir " + file.getPath() + "/");
                data.getDataList().stream().map(Data::dump).forEach(this::add);
            }});
        }

        @Override
        public String dump(String path, InspectorCache caches) {
            return String.join("\n", new ArrayList<String>() {{
                add(file.getName() + "/");
                data.getDataList().stream().map(Data::dump).map(TextUtil::indent).forEach(this::add);
            }});
        }
    }
}
