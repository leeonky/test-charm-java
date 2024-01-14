package com.github.leeonky.dal.extensions.basic;

import com.github.leeonky.dal.extensions.basic.sync.Await;
import com.github.leeonky.dal.extensions.basic.sync.Eventually;
import com.github.leeonky.util.Suppressor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.github.leeonky.dal.Assertions.expect;
import static com.github.leeonky.dal.Assertions.expectRun;

public class SyncTest {

    @Nested
    class EventuallyTest {

        @BeforeEach
        void reset() {
            Eventually.setDefaultWaitingTime(5000);
        }

        @Test
        void pass_right_now() {
            expect(new HashMap<String, Object>() {{
                put("value", 1);
            }}).should("::eventually: {value= 1}");
        }

        @Test
        void pass_in_1_second() {
            expect(new Target(1)).should("::eventually: {value= done}");
        }

        @Test
        void set_waiting_time_in_dal() {
            Eventually.setDefaultWaitingTime(100);
            expect(new Target(1)).should("::eventually.within(2s): {value= done}");
        }

        @Test
        void set_interval_time_in_dal() {
            expectRun(() ->
                    expect(new TargetList()).should("::eventually.within(3s).interval(1.8s): {value::filter(1): {id= 1}}"))
                    .should("::throw.message= /.*There are only 0 elements, try again.*/");
        }
    }

    @Nested
    public class AwaitTest {

        @BeforeEach
        void reset() {
            Await.setDefaultWaitingTime(5000);
        }

        @Test
        void raise_error_when_no_matches_list() {
            Await.setDefaultWaitingTime(2000);
            expectRun(() -> expect(new TargetList()).should("::await: {value::filter!: {id= 'not-exist'}}"))
                    .should("::throw.message= /.*Filtered result is empty, try again.*/");
        }

        @Test
        void return_filtered_result_at_last() {
            expect(new TargetList()).should("::await: {value::filter!: {id= 1}: [* ...]}");
        }

        @Test
        void set_waiting_time_in_dal() {
            expectRun(() -> expect(new TargetList()).should("::await.within(100ms): {value::filter!: {id= 1}}"))
                    .should("::throw.message= /.*Filtered result is empty, try again.*/");
        }

        @Test
        void set_interval_time_in_dal() {
            expectRun(() ->
                    expect(new TargetList()).should("::await.within(3s).interval(1.8s): {value::filter(1): {id= 1}}"))
                    .should("::throw.message= /.*There are only 0 elements, try again.*/");
        }
    }

    public static class Target {
        private String value = "";

        public Target(int second) {
            new Thread(() -> {
                Suppressor.run(() -> Thread.sleep(second * 1000));
                value = "done";
            }).start();
        }

        public String getValue() {
            return value;
        }
    }

    public static class TargetList {
        private int i = 0;
        private final List<Object> values = new ArrayList<>();

        public TargetList() {
            new Thread(() -> {
                while (true) {
                    Suppressor.run(() -> Thread.sleep(1000));
                    values.add(new HashMap<Object, Object>() {{
                        put("id", i++ / 2);
                        put("value", "string");
                    }});
                }
            }).start();
        }

        public List<Object> getValue() {
            return values;
        }
    }
}
