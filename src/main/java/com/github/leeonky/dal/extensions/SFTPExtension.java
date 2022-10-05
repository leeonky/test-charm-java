package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.PropertyAccessor;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SFTPExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        //                        TODO refactor
        dal.getRuntimeContextBuilder()
                .registerListAccessor(SFtpFile.class, SFtpFile::ls)
                .registerPropertyAccessor(SFtpFile.class, new PropertyAccessor<SFtpFile>() {
                    @Override
                    public Object getValue(SFtpFile sFtpFile, Object property) {
                        return sFtpFile.access(property);
                    }

                    @Override
                    public Set<Object> getPropertyNames(SFtpFile sFtpFile) {
                        return sFtpFile.ls().stream().map(SFtpFile::name)
                                .collect(Collectors.toCollection(LinkedHashSet::new));
                    }

                    @Override
                    public boolean isNull(SFtpFile instance) {
                        return instance == null;
                    }
                });
        dal.getRuntimeContextBuilder().registerObjectDumper(SFtpFile.class, SFtpFile::name);
    }
}
