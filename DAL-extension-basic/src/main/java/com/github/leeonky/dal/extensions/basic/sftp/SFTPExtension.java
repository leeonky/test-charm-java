package com.github.leeonky.dal.extensions.basic.sftp;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.basic.sftp.util.SFtpFile;
import com.github.leeonky.dal.extensions.basic.sftp.util.SFtpFileJavaClassPropertyAccessor;
import com.github.leeonky.dal.extensions.basic.sftp.util.Util;
import com.github.leeonky.dal.runtime.CollectionDALCollection;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;

@SuppressWarnings("unused")
public class SFTPExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        RuntimeContextBuilder builder = dal.getRuntimeContextBuilder();
        builder.registerImplicitData(SFtpFile.class, SFtpFile::download)
                .registerDALCollectionFactory(SFtpFile.class, sFtpFile -> new CollectionDALCollection<>(sFtpFile.ls()))
                .registerPropertyAccessor(SFtpFile.class, new SFtpFileJavaClassPropertyAccessor())
                .registerDumper(SFtpFile.class, data -> ((SFtpFile) data.instance()).isDir()
                        ? Util.DIR_DUMPER : Util.FILE_DUMPER);
    }
}
