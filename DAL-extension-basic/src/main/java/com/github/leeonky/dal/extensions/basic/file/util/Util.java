package com.github.leeonky.dal.extensions.basic.file.util;

import com.github.leeonky.util.Sneaky;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;

public class Util {
    public static final FileDirDumper FILE_DIR_DUMPER = new FileDirDumper();
    public static final FileFileDumper FILE_FILE_DUMPER = new FileFileDumper();
    public static final PathDirDumper PATH_DIR_DUMPER = new PathDirDumper();
    public static final PathFileDumper PATH_FILE_DUMPER = new PathFileDumper();

    public static String formatFileSize(long size) {
        if (size < 10000)
            return String.valueOf(size);
        double sizeInUnit = size / 1024.0;
        if (sizeInUnit < 1000)
            return format("%.1fK", sizeInUnit);
        sizeInUnit /= 1024;
        if (sizeInUnit < 1000)
            return format("%.1fM", sizeInUnit);
        sizeInUnit /= 1024;
        if (sizeInUnit < 1000)
            return format("%.1fG", sizeInUnit);
        sizeInUnit /= 1024;
        return format("%.1fT", sizeInUnit);
    }

    public static String attribute(Path path) {
        PosixFileAttributes posixFileAttributes = Sneaky.get(() -> Files.readAttributes(path, PosixFileAttributes.class));
        return format("%s %s %s %6s %s %s", PosixFilePermissions.toString(posixFileAttributes.permissions()),
                posixFileAttributes.group(), posixFileAttributes.owner(), formatFileSize(path.toFile().length()),
                posixFileAttributes.lastModifiedTime(), path.getFileName().toString());
    }

    public static Object getSubFile(File file, String name) {
        File subFile = new File(file, name);
        if (subFile.exists())
            return subFile;
        String[] list = file.list();
        if (list != null && stream(list).anyMatch(f -> f.startsWith(name + ".")))
            return new IOFileFileGroup(file, name);
        return Sneaky.sneakyThrow(new FileNotFoundException(format("File or File Group <%s> not found", name)));
    }

    public static Set<String> listFileNames(File file) {
        return listFile(file).stream().map(File::getName).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static Set<File> listFile(File file) {
        File[] files = file.listFiles();
        return files == null ? Collections.emptySet() : stream(files)
                .sorted(comparing(File::isDirectory).thenComparing(File::getName))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static Set<Path> listFile(Path path) {
        File[] files = path.toFile().listFiles();
        return (files == null ? Collections.emptySet() : stream(files)
                .sorted(comparing(File::isDirectory).thenComparing(File::getName)).map(File::toPath)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }
}
