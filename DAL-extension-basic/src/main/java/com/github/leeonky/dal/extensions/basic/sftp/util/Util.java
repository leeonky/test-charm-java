package com.github.leeonky.dal.extensions.basic.sftp.util;

import com.github.leeonky.dal.runtime.inspector.Dumper;

import java.io.FileNotFoundException;
import java.util.Optional;

import static com.github.leeonky.util.Sneaky.sneakyThrow;

public class Util {
    public static final Dumper DIR_DUMPER = new DirDumper();
    public static final Dumper FILE_DUMPER = new FileDumper();

    public static Object getSubFile(SFtpFile sFtpFile, Object property) {
        Optional<SFtpFile> first = sFtpFile.access(property);
        if (first.isPresent())
            return first.get();
        if (sFtpFile.ls().stream().anyMatch(f -> f.name().startsWith(property + ".")))
            return new SftpFileGroup(sFtpFile, property.toString());
        return sneakyThrow(new FileNotFoundException(String.format("File or File Group <%s> not found", property)));
    }
}
