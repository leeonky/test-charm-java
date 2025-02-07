package com.github.leeonky.dal.extensions.inspector;

import com.github.leeonky.dal.DAL;

public class Inspector {
    private static final Inspector instance = new Inspector();
    private HttpServer server;
    private Mode defaultMode;
    private InspectorContext inspectorContext;

    public static Inspector inspector() {
        return instance;
    }

    public void setDefaultMode(Mode mode) {
        defaultMode = mode;
    }

    public void launch() {
        synchronized (Inspector.class) {
            if (server == null) {
                inspectorContext = new InspectorContext();
                server = new HttpServer(inspectorContext.apiProvider()).start();
            }
        }
    }

    public Inspector exit() {
        synchronized (Inspector.class) {
            if (server != null) {
                server.stop();
                server = null;
            }
        }
        return this;
    }

    public void inspect(DAL dal, Object input, String code) {
        if (isRecursive())
            return;
        inspectorContext.inspect(dal, input, code);
    }

    private boolean isRecursive() {
        for (StackTraceElement stack : Thread.currentThread().getStackTrace())
            if (InspectorContext.ApiProvider.class.getName().equals(stack.getClassName()))
                return true;
        return false;
    }

    public enum Mode {
        DAL_INSPECTOR_ASSERT_DISABLED, DAL_INSPECTOR_AUTO, DAL_INSPECTOR_FORCED
    }
}
