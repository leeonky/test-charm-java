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

        private Executor(DAL dal) {
            this.dal = dal;
        }

        public void evaluate(String code) {
            lastEvaluating = code;
            running = true;
            startedAt = Instant.now();
            Thread thread = new Thread(() -> {
                try {
                    dal.evaluate(input, code);
                } catch (Throwable e) {
                    lastThrow = e;
                } finally {
                    running = false;
                }
            });
//            TODO exit thread
            thread.start();
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

//    private final DAL dal;
//    private Object givenData;
//    private String lastEvaluating;
//    private Throwable lastThrow;
//    private boolean running;
//
//    private Instant startedAt;
//
//    public TestContext() {
//        Inspector.shutdown();
//        Inspector.launch();
//        Inspector.getInstances().clear();
//        dal = DAL.create("Test");
//    }
//
//    public void changeInspectorMode(String mode) {
//        InspectorCore.setMode(InspectorCore.Mode.valueOf(mode));
//    }
//
//    public void givenData(Object data) {
//        givenData = data;
//    }
//
//    public void evaluating(String expression) {
//        lastEvaluating = expression;
//        running = true;
//        startedAt = Instant.now();
//        Thread thread = new Thread(() -> {
//            try {
//                dal.evaluate(givenData, expression);
//            } catch (Throwable e) {
//                lastThrow = e;
//            } finally {
//                running = false;
//            }
//        });
//        thread.start();
//    }
//
//    @SneakyThrows
//    public void shouldStillRunningAfter(int ms) {
//        long wait = ms - (Instant.now().toEpochMilli() - startedAt.toEpochMilli());
//        if (wait > 0)
//            Thread.sleep(ms);
//        if (!running)
//            fail("Test ended");
//    }
//
//    public String lastEvaluating() {
//        return lastEvaluating;
//    }
//
//    public void shouldFailedWith(String error) {
//        await().ignoreExceptions().untilAsserted(() -> assertThat(running).isFalse());
//        assertThat(lastThrow.getMessage()).isEqualTo(error);
//    }
//
//    public void evaluatingAnother(String name, String expression) {
//        Thread thread = new Thread(() -> {
//            try {
//                DAL.create(name).evaluate(givenData, expression);
//            } catch (Throwable e) {
//                lastThrow = e;
//            } finally {
//                running = false;
//            }
//        });
//        thread.start();
//    }
}
