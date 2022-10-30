package com.github.leeonky.dal.extensions.basic.sftp.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingContext;

public class FileDumper implements Dumper {

    @Override
    public void dumpDetail(Data path, DumpingContext context) {
        SFtpFile sFtpFile = (SFtpFile) path.getInstance();
        context.append(sFtpFile.remoteInfo()).newLine();
        dump(path, context);
    }

    @Override
    public void dump(Data data, DumpingContext context) {
        SFtpFile sFtpFile = (SFtpFile) data.getInstance();
        context.append(sFtpFile.attribute()).append(" ").append(sFtpFile.name());
    }
}
