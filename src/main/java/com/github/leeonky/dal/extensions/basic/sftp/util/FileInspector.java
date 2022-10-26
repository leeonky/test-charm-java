package com.github.leeonky.dal.extensions.basic.sftp.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.Inspector;
import com.github.leeonky.dal.runtime.inspector.InspectorContext;

class FileInspector implements Inspector {
    @Override
    public String inspect(Data path, InspectorContext cache) {
        SFtpFile sFtpFile1 = (SFtpFile) path.getInstance();
        return sFtpFile1.attribute() + " " + sFtpFile1.name();
    }
}
