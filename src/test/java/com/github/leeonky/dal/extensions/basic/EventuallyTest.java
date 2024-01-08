package com.github.leeonky.dal.extensions.basic;

import com.github.leeonky.dal.extensions.basic.sync.Eventually;
import com.github.leeonky.util.Suppressor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.github.leeonky.dal.Assertions.expect;

public class EventuallyTest {
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
        expect(new Target(2)).should("::eventually.interval(1s): {value= done}");
    }

//    @Test
//    void eventually_filter() {
////        expect(new TargetList()).should("::eventually: {value: [... {id: 2, value: int}]}");
//
//        expect(new TargetList()).should("" +
//                                        "::eventually: {value: [...{id: 3}...]}" +
//                                        "= 1" +
//                                        "");
//    }

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
                        put("id", i++);
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
