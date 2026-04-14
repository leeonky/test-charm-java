package org.testcharm.dal.extensions.basic.sftp.util;

import org.testcharm.dal.runtime.JavaClassPropertyAccessor;

import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.stream.Collectors.toCollection;

public class SFtpFileJavaClassPropertyAccessor extends JavaClassPropertyAccessor<SFtpFile> {

    @Override
    public Object getValue(SFtpFile sFtpFile, Object property) {
        return sFtpFile.isDir() ? Util.getSubFile(sFtpFile, property) : super.getValue(sFtpFile, property);
    }

    @Override
    public Set<?> getPropertyNames(SFtpFile sFtpFile) {
        return sFtpFile.isDir() ? sFtpFile.ls().stream().map(SFtpFile::name)
                .collect(toCollection(LinkedHashSet::new)) : super.getPropertyNames(sFtpFile);
    }
}
