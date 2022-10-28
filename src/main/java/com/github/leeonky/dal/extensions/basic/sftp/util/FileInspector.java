package com.github.leeonky.dal.extensions.basic.sftp.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.Inspector;
import com.github.leeonky.dal.runtime.inspector.InspectorContext;

//TODO refactor
class FileInspector implements Inspector {
    @Override
    public String inspect(Data path, InspectorContext context) {
        SFtpFile sFtpFile = (SFtpFile) path.getInstance();
        return sFtpFile.remoteInfo() + "\n" + dump(path, context);
    }

    @Override
    public String dump(Data data, InspectorContext context) {
        SFtpFile sFtpFile = (SFtpFile) data.getInstance();
        return sFtpFile.attribute() + " " + sFtpFile.name();
    }
}
