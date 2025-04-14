package com.github.leeonky.dal.extensions.basic.sftp.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;

public class FileDumper implements Dumper<SFtpFile> {

    @Override
    public void dump(Data<SFtpFile> path, DumpingBuffer context) {
        context.append(path.instance().remoteInfo()).newLine();
        dumpValue(path, context);
    }

    @Override
    public void dumpValue(Data<SFtpFile> data, DumpingBuffer context) {
        context.append(data.instance().attribute()).append(" ").append(data.instance().name());
    }
}
