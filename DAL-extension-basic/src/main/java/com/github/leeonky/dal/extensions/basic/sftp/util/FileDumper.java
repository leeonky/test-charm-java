package com.github.leeonky.dal.extensions.basic.sftp.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;

public class FileDumper implements Dumper<SFtpFile> {

    @Override
    public void dump(Data<SFtpFile> path, DumpingBuffer context) {
        context.append(path.value().remoteInfo()).newLine();
        dumpValue(path, context);
    }

    @Override
    public void dumpValue(Data<SFtpFile> data, DumpingBuffer context) {
        context.append(data.value().attribute()).append(" ").append(data.value().name());
    }
}
