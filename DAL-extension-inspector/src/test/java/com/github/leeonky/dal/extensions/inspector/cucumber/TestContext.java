package com.github.leeonky.dal.extensions.inspector.cucumber;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.inspector.Inspector;
import lombok.SneakyThrows;

import static org.assertj.core.api.Assertions.fail;

public class TestContext {
    private final DAL dal;
    private Object givenData;
    private String lastEvaluating;
    private Throwable lastThrow;
    private boolean running;

    public TestContext() {
        Inspector.inspector().setDefaultMode(null);
        dal = DAL.getInstance();
    }

    public void changeInspectorMode(String mode) {
        Inspector.inspector().setDefaultMode(Inspector.Mode.valueOf(mode));
    }

    public void givenData(Object data) {
        givenData = data;
    }

    public void evaluating(String expression) {
        lastEvaluating = expression;
        running = true;
        Thread thread = new Thread(() -> {
            try {
                dal.evaluate(givenData, expression);
            } catch (Throwable e) {
                lastThrow = e;
            } finally {
                running = false;
            }
        });
        thread.start();
    }

    @SneakyThrows
    public void shouldStillRunningAfter(int ms) {
        Thread.sleep(ms);
        if (!running) {
            fail("Test ended");
        }
    }

    public String lastEvaluating() {
        return lastEvaluating;
    }
}
