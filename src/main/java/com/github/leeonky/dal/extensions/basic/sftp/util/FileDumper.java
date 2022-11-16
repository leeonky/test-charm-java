package com.github.leeonky.dal.extensions.basic.sftp.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;

public class FileDumper implements Dumper {

    @Override
    public void dump(Data path, DumpingBuffer context) {
        SFtpFile sFtpFile = (SFtpFile) path.getInstance();
        context.append(sFtpFile.remoteInfo()).newLine();
        dumpValue(path, context);
    }

    @Override
    public void dumpValue(Data data, DumpingBuffer context) {
        SFtpFile sFtpFile = (SFtpFile) data.getInstance();
        context.append(sFtpFile.attribute()).append(" ").append(sFtpFile.name());
    }
}
