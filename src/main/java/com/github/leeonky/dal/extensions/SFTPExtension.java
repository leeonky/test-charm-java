package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.JavaClassPropertyAccessor;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.InvocationException;

import java.io.FileNotFoundException;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toCollection;

public class SFTPExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        RuntimeContextBuilder builder = dal.getRuntimeContextBuilder();
        builder.registerImplicitData(SFtp.SubSFtpFile.class, SFtp.SubSFtpFile::download)
                .registerListAccessor(SFtpFile.class, SFtpFile::ls)
                .registerPropertyAccessor(SFtpFile.class,
                        new JavaClassPropertyAccessor<SFtpFile>(builder, BeanClass.create(SFtpFile.class)) {
                            @Override
                            public Object getValue(SFtpFile sFtpFile, Object property) {
                                return sFtpFile.isDir() ? getSubFile(sFtpFile, property) : super.getValue(sFtpFile, property);
                            }

                            @Override
                            public Set<Object> getPropertyNames(SFtpFile sFtpFile) {
                                return sFtpFile.isDir() ? sFtpFile.ls().stream().map(SFtpFile::name)
                                        .collect(toCollection(LinkedHashSet::new)) : super.getPropertyNames(sFtpFile);
                            }

                            @Override
                            public boolean isNull(SFtpFile instance) {
                                return instance == null;
                            }
                        })
                .registerObjectDumper(SFtpFile.class, SFtpFile::name)
        ;
    }

    private Object getSubFile(SFtpFile sFtpFile, Object property) {
        Optional<SFtpFile> first = sFtpFile.access(property);
        if (first.isPresent())
            return first.get();
        if (sFtpFile.ls().stream().anyMatch(f -> f.name().startsWith(property + ".")))
            return new SftpFileGroup(sFtpFile, property.toString());
        throw new InvocationException(new FileNotFoundException(String.format("File or File Group <%s> not found", property)));
    }
}
