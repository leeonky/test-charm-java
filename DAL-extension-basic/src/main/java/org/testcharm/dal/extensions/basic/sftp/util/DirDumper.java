package org.testcharm.dal.extensions.basic.sftp.util;

import org.testcharm.dal.runtime.Data;
import org.testcharm.dal.runtime.inspector.Dumper;
import org.testcharm.dal.runtime.inspector.DumpingBuffer;

public class DirDumper implements Dumper<SFtpFile> {

    @Override
    public void dump(Data<SFtpFile> data, DumpingBuffer context) {
        DumpingBuffer sub = context.append(data.value().remoteInfo()).fork();
        data.list().wraps().values().forEach(subFile -> sub.newLine().dumpValue(subFile));
    }

    @Override
    public void dumpValue(Data<SFtpFile> data, DumpingBuffer context) {
        DumpingBuffer sub = context.append(data.value().name()).append("/").indent();
        data.list().wraps().values().forEach(subFile -> sub.newLine().dumpValue(subFile));
    }
}
