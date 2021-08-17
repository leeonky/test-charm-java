package com.github.leeonky.dal.ast;

import com.github.leeonky.dal.RuntimeContext;
import com.github.leeonky.dal.RuntimeContextBuilder;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class ListNodeTest {

    public static final Operator.Equal OPERATOR = new Operator.Equal();
    public static final Operator.Matcher MATCHER = new Operator.Matcher();
    RuntimeContext runtimeContext = new RuntimeContextBuilder().build(null);
    ListNode listNode = new ListNode();

    @Test
    void empty_list_equal_to_empty_list() {
        assertThat(listNode.judge(new ConstNode(Collections.emptyList()), OPERATOR, runtimeContext)).isTrue();
    }
}