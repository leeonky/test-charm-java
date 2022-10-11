package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.JavaClassPropertyAccessor;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.dal.runtime.inspector.ListInspector;
import com.github.leeonky.dal.runtime.inspector.TypeValueInspector;
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
                        new JavaClassPropertyAccessor<SFtpFile>(BeanClass.create(SFtpFile.class)) {
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
                .registerInspector(SFtpFile.class, data -> {
                    SFtpFile sFtpFile = (SFtpFile) data.getInstance();
                    if (sFtpFile.isDir()) {
                        return (path, inspectorCache) -> sFtpFile.name() + "/: " + new ListInspector(data) {
                            @Override
                            protected String type() {
                                return "";
                            }
                        }.inspect(path, inspectorCache);
                    } else {
                        return new TypeValueInspector() {
                            @Override
                            public String inspectType() {
                                return sFtpFile.name();
                            }

                            @Override
                            public String inspectValue() {
                                return ((SFtp.SubSFtpFile) sFtpFile).attribute();
                            }
                        };
                    }
                });
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
