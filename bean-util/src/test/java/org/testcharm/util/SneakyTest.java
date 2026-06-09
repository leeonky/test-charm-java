package org.testcharm.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.function.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SneakyTest {

    @Test
    void return_from_code_block() {
        assertThat(Sneaky.get(() -> 1)).isEqualTo(1);
    }

    @Test
    void throw_exception() {
        Exception exception = new Exception();
        assertThat(assertThrows(Exception.class, () -> Sneaky.get(() -> {
            throw exception;
        }))).isEqualTo(exception);
    }

    private boolean called = false;

    @Test
    void run_return_void() {
        Sneaky.run(() -> called = true);

        assertThat(called).isTrue();
    }

    @Test
    void run_throw_exception() {
        Exception exception = new Exception();
        assertThat(assertThrows(Exception.class, () -> Sneaky.run(() -> {
            throw exception;
        }))).isEqualTo(exception);
    }

    @Test
    void execute_should_catch_InvocationTargetException_and_rethrow() {
        Exception exception = new Exception();
        assertThat(assertThrows(Exception.class, () -> Sneaky.execute(() -> {
            throw new InvocationTargetException(exception);
        }))).isEqualTo(exception);
    }

    @Test
    void execute_throw_exception() {
        Exception exception = new Exception();
        assertThat(assertThrows(Exception.class, () -> Sneaky.execute(() -> {
            throw exception;
        }))).isEqualTo(exception);
    }

    @Test
    void execute_throwable_run() {
        Sneaky.executeVoid(() -> called = true);
        assertTrue(called);
    }

    @Test
    void execute_void_should_catch_InvocationTargetException_and_rethrow() {
        Exception exception = new Exception();
        assertThat(assertThrows(Exception.class, () -> Sneaky.executeVoid(() -> {
            throw new InvocationTargetException(exception);
        }))).isEqualTo(exception);
    }

    @Test
    void sneaky_get_supplier_return_value() {
        Supplier<Integer> supplier = Sneaky.sneakyGet(() -> 1);

        assertThat(supplier.get()).isEqualTo(1);
    }

    @Test
    void sneaky_get_supplier_throw_exception() {
        Exception exception = new Exception();
        Supplier<Object> supplier = Sneaky.sneakyGet(() -> {
            throw exception;
        });

        assertThat(assertThrows(Exception.class, supplier::get)).isEqualTo(exception);
    }

    @Test
    void sneaky_get_function_return_value() {
        Function<Integer, Integer> fun = Sneaky.sneakyGet(a -> a + 1);

        assertThat(fun.apply(1)).isEqualTo(2);
    }

    @Test
    void sneaky_get_function_pass_argument_and_throw_exception() {
        int[] calledWith = new int[1];
        Exception exception = new Exception();
        Function<Integer, Object> fun = Sneaky.sneakyGet(a -> {
            calledWith[0] = a;
            throw exception;
        });

        assertThat(assertThrows(Exception.class, () -> fun.apply(10))).isEqualTo(exception);
        assertThat(calledWith[0]).isEqualTo(10);
    }

    @Test
    void sneaky_get_bi_function_return_value() {
        BiFunction<Integer, Integer, Integer> fun = Sneaky.sneakyGet((a1, a2) -> a1 + a2);

        assertThat(fun.apply(1, 2)).isEqualTo(3);
    }

    @Test
    void sneaky_get_bi_function_pass_arguments_and_throw_exception() {
        int[] calledWith = new int[2];
        Exception exception = new Exception();
        BiFunction<Integer, Integer, Object> fun = Sneaky.sneakyGet((a1, a2) -> {
            calledWith[0] = a1;
            calledWith[1] = a2;
            throw exception;
        });

        assertThat(assertThrows(Exception.class, () -> fun.apply(10, 20))).isEqualTo(exception);
        assertThat(calledWith[0]).isEqualTo(10);
        assertThat(calledWith[1]).isEqualTo(20);
    }

    @Test
    void sneaky_run_runnable_should_run() {
        Runnable runnable = Sneaky.sneakyRun(() -> called = true);

        runnable.run();

        assertThat(called).isTrue();
    }

    @Test
    void sneaky_run_runnable_should_throw_exception() {
        Exception exception = new Exception();
        Runnable runnable = Sneaky.sneakyRun(() -> {
            throw exception;
        });

        assertThat(assertThrows(Exception.class, runnable::run)).isEqualTo(exception);
    }

    @Test
    void sneaky_run_consumer_should_accept() {
        int[] calledWith = new int[1];
        Consumer<Integer> consumer = Sneaky.sneakyRun(a -> calledWith[0] = a);

        consumer.accept(1);

        assertThat(calledWith[0]).isEqualTo(1);
    }

    @Test
    void sneaky_run_consumer_should_pass_argument_and_throw_exception() {
        int[] calledWith = new int[1];
        Exception exception = new Exception();
        Consumer<Integer> consumer = Sneaky.sneakyRun(a -> {
            calledWith[0] = a;
            throw exception;
        });

        assertThat(assertThrows(Exception.class, () -> consumer.accept(10))).isEqualTo(exception);
        assertThat(calledWith[0]).isEqualTo(10);
    }

    @Test
    void sneaky_run_bi_consumer_should_accept() {
        int[] calledWith = new int[2];
        BiConsumer<Integer, Integer> consumer = Sneaky.sneakyRun((a1, a2) -> {
            calledWith[0] = a1;
            calledWith[1] = a2;
        });

        consumer.accept(1, 2);

        assertThat(calledWith[0]).isEqualTo(1);
        assertThat(calledWith[1]).isEqualTo(2);
    }

    @Test
    void sneaky_run_bi_consumer_should_pass_arguments_and_throw_exception() {
        int[] calledWith = new int[2];
        Exception exception = new Exception();
        BiConsumer<Integer, Integer> consumer = Sneaky.sneakyRun((a1, a2) -> {
            calledWith[0] = a1;
            calledWith[1] = a2;
            throw exception;
        });

        assertThat(assertThrows(Exception.class, () -> consumer.accept(10, 20))).isEqualTo(exception);
        assertThat(calledWith[0]).isEqualTo(10);
        assertThat(calledWith[1]).isEqualTo(20);
    }
}
