package com.github.leeonky.dal.extensions.basic.sftp.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;

public class DirDumper implements Dumper {

    @Override
    public void dump(Data data, DumpingBuffer context) {
        DumpingBuffer sub = context.append(((SFtpFile) data.instance()).remoteInfo()).sub();
        data.resolved().eachSubData(subFile -> sub.newLine().dumpValue(subFile));
    }

    @Override
    public void dumpValue(Data data, DumpingBuffer context) {
        DumpingBuffer sub = context.append(((SFtpFile) data.instance()).name()).append("/").indent();
        data.resolved().eachSubData(subFile -> sub.newLine().dumpValue(subFile));
    }
}
