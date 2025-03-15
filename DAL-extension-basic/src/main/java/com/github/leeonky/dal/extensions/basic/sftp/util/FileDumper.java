package com.github.leeonky.dal.extensions.basic.sftp.util;

import com.github.leeonky.dal.runtime.Data.Resolved;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;

public class FileDumper implements Dumper {

    @Override
    public void dump(Resolved path, DumpingBuffer context) {
        SFtpFile sFtpFile = path.value();
        context.append(sFtpFile.remoteInfo()).newLine();
        dumpValue(path, context);
    }

    @Override
    public void dumpValue(Resolved data, DumpingBuffer context) {
        SFtpFile sFtpFile = data.value();
        context.append(sFtpFile.attribute()).append(" ").append(sFtpFile.name());
    }
}
