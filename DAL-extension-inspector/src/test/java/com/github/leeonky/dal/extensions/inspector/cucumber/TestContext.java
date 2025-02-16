package com.github.leeonky.dal.extensions.inspector.cucumber;

public class TestContext {
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
