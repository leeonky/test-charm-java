package com.github.leeonky.dal.extensions.basic.sftp;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.basic.sftp.util.SFtpFile;
import com.github.leeonky.dal.extensions.basic.sftp.util.Util;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.JavaClassPropertyAccessor;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.util.BeanClass;

import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.stream.Collectors.toCollection;

@SuppressWarnings("unused")
public class SFTPExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        RuntimeContextBuilder builder = dal.getRuntimeContextBuilder();
        builder.registerImplicitData(SFtpFile.class, SFtpFile::download)
                .registerListAccessor(SFtpFile.class, SFtpFile::ls)
                .registerPropertyAccessor(SFtpFile.class, new SFtpFileJavaClassPropertyAccessor())
                .registerDumper(SFtpFile.class, data -> ((SFtpFile) data.getInstance()).isDir()
                        ? Util.DIR_DUMPER : Util.FILE_DUMPER);
    }

    private class SFtpFileJavaClassPropertyAccessor extends JavaClassPropertyAccessor<SFtpFile> {
        public SFtpFileJavaClassPropertyAccessor() {
            super(BeanClass.create(SFtpFile.class));
        }

        @Override
        public Object getValue(SFtpFile sFtpFile, Object property) {
            return sFtpFile.isDir() ? Util.getSubFile(sFtpFile, property) : super.getValue(sFtpFile, property);
        }

        @Override
        public Set<Object> getPropertyNames(SFtpFile sFtpFile) {
            return sFtpFile.isDir() ? sFtpFile.ls().stream().map(SFtpFile::name)
                    .collect(toCollection(LinkedHashSet::new)) : super.getPropertyNames(sFtpFile);
        }
    }
}
