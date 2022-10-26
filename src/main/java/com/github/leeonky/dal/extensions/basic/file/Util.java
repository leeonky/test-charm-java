package com.github.leeonky.dal.extensions.basic.file;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;

import static com.github.leeonky.util.Suppressor.get;
import static java.lang.String.format;

public class Util {
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
        PosixFileAttributes posixFileAttributes = get(() -> Files.readAttributes(path, PosixFileAttributes.class));
        return format("%s %s %s %6s %s %s", PosixFilePermissions.toString(posixFileAttributes.permissions()),
                posixFileAttributes.group(), posixFileAttributes.owner(), formatFileSize(path.toFile().length()),
                posixFileAttributes.lastModifiedTime(), path.getFileName().toString());
    }
}
