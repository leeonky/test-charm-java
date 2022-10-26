package com.github.leeonky.dal.extensions.basic.sftp.util;

import com.github.leeonky.util.InvocationException;

import java.io.FileNotFoundException;
import java.util.Optional;

public class Util {
    public static final DirInspector DIR_INSPECTOR = new DirInspector();
    public static final FileInspector FILE_INSPECTOR = new FileInspector();

    public static Object getSubFile(SFtpFile sFtpFile, Object property) {
        Optional<SFtpFile> first = sFtpFile.access(property);
        if (first.isPresent())
            return first.get();
        if (sFtpFile.ls().stream().anyMatch(f -> f.name().startsWith(property + ".")))
            return new SftpFileGroup(sFtpFile, property.toString());
        throw new InvocationException(new FileNotFoundException(String.format("File or File Group <%s> not found", property)));
    }
}
