package com.github.leeonky.dal.extensions.basic.sftp.util;

import com.github.leeonky.dal.runtime.Data.Resolved;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;

public class DirDumper implements Dumper {

    @Override
    public void dump(Resolved data, DumpingBuffer context) {
        DumpingBuffer sub = context.append(((SFtpFile) data.value()).remoteInfo()).sub();
        data.eachSubData(subFile -> sub.newLine().dumpValue(subFile));
    }

    @Override
    public void dumpValue(Resolved data, DumpingBuffer context) {
        DumpingBuffer sub = context.append(((SFtpFile) data.value()).name()).append("/").indent();
        data.eachSubData(subFile -> sub.newLine().dumpValue(subFile));
    }
}
