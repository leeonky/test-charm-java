package com.github.leeonky.dal.extensions.inspector;

import com.github.leeonky.dal.DAL;

import static com.github.leeonky.util.function.Extension.getFirstPresent;
import static java.util.Optional.ofNullable;

public class Inspector {
    private static final Inspector instance = new Inspector();
    private HttpServer server;
    private Mode mode;
    private InspectorContext inspectorContext;

    public static Inspector inspector() {
        return instance;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
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

    public void inspectViaMode(DAL dal, Object input, String code) {
        if (currentMode() == Mode.DAL_INSPECTOR_ASSERT_FORCED)
            inspect(dal, input, code);
    }

    public void inspect(DAL dal, Object input, String code) {
        if (!isRecursive())
            inspectorContext.inspect(dal, input, code);
    }

    public Mode currentMode() {
        return getFirstPresent(() -> ofNullable(mode),
                () -> ofNullable(System.getenv("DAL_INSPECTOR_ASSERT_MODE")).map(Mode::valueOf),
                () -> ofNullable(System.getProperty("dal.inspector.assert-mode")).map(Mode::valueOf))
                .orElse(Mode.DAL_INSPECTOR_ASSERT_DISABLED);
    }

    private boolean isRecursive() {
        for (StackTraceElement stack : Thread.currentThread().getStackTrace())
            if (InspectorContext.ApiProvider.class.getName().equals(stack.getClassName()))
                return true;
        return false;
    }

    public enum Mode {
        DAL_INSPECTOR_ASSERT_DISABLED, DAL_INSPECTOR_ASSERT_FORCED
    }
}
