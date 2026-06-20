package org.testcharm.dal.extensions.inspector.cucumber;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.testcharm.dal.DAL;
import org.testcharm.dal.extensions.inspector.InspectorExtension;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;

public class TestContext {
    private final Map<String, Executor> executors = new HashMap<>();
    private final Map<String, Object> constantsMap = new HashMap<>();
    private final DAL dal = DAL.create("Default", InspectorExtension.class);

    public void addInput(String dalIns, Object data) {
        executors.get(dalIns).setInput(data);
    }

    public void evaluate(String dalIns, String code) {
        executors.get(dalIns).evaluate(code, constantsMap.get(dalIns));
    }

    public void createDAL(String name) {
        executors.put(name, new Executor(DAL.create(name)));
    }

    public void shouldStillRunningAfter(String dalIns, float second) {
        executors.get(dalIns).shouldStillRunningAfter((int) (second * 1000));
    }

    public Object resultOf(String dalIns) {
        Executor executor = executors.get(dalIns);
        await().ignoreExceptions().untilAsserted(() -> assertThat(executor.running).isFalse());
        if (executor.throwing != null)
            return executor.throwing;
        return executor.result;
    }

    public void addConstants(String dalIns, Object constants) {
        constantsMap.put(dalIns, constants);
    }

    public static class Executor {
        private final DAL dal;
        @Setter
        private Object input;

        @Getter
        private String lastEvaluating;
        private Throwable throwing;
        private boolean running;
        private Instant startedAt;
        private Thread thread;
        private Object result;

        private Executor(DAL dal) {
            this.dal = dal;
        }

        public void evaluate(String code, Object constants) {
            lastEvaluating = code;
            running = true;
            startedAt = Instant.now();
            thread = new Thread(() -> {
                try {
                    result = dal.evaluate(() -> input, code, null, constants);
                } catch (Throwable e) {
                    throwing = e;
                } finally {
                    running = false;
                }
            });
            thread.start();
        }

        @Override
        protected void finalize() throws Throwable {
            thread.interrupt();
            super.finalize();
        }

        @SneakyThrows
        public void shouldStillRunningAfter(int ms) {
            long wait = ms - (Instant.now().toEpochMilli() - startedAt.toEpochMilli());
            if (wait > 0)
                Thread.sleep(ms);
            if (!running)
                fail("Test ended");
        }
    }
}
