package com.github.leeonky.dal.extensions.inspector.cucumber;

import com.github.leeonky.dal.DAL;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.github.leeonky.dal.extensions.basic.text.Methods.json;
import static org.assertj.core.api.Assertions.fail;

public class TestContext {
    private final Map<String, Executor> executors = new HashMap<>();

    public void addInput(String dalIns, String inputJson) {
        executors.get(dalIns).setInput(json(inputJson));
    }

    public void evaluate(String dalIns, String code) {
        executors.get(dalIns).evaluate(code);
    }

    public void createDAL(String name) {
        executors.put(name, new Executor(DAL.create(name)));
    }

    public void shouldShowTheFollowingError(String dalIns, float second) {
        executors.get(dalIns).shouldStillRunningAfter((int) (second * 1000));
    }

    public static class Executor {
        private final DAL dal;
        @Setter
        private Object input;

        @Getter
        private String lastEvaluating;
        private Throwable lastThrow;
        private boolean running;
        private Instant startedAt;
        private Thread thread;

        private Executor(DAL dal) {
            this.dal = dal;
        }

        public void evaluate(String code) {
            lastEvaluating = code;
            running = true;
            startedAt = Instant.now();
            thread = new Thread(() -> {
                try {
                    dal.evaluate(input, code);
                } catch (Throwable e) {
                    lastThrow = e;
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
