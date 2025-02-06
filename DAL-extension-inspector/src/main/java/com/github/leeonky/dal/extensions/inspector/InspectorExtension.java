package com.github.leeonky.dal.extensions.inspector;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;

public class InspectorExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        Server.INSTANCE.start();
    }
}
