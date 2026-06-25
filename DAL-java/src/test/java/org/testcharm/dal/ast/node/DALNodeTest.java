package org.testcharm.dal.ast.node;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DALNodeTest {

    @Test
    void to_string_return_inspect_value() {
        DALNode node = new DALNode() {
            @Override
            public String inspect() {
                return "inspect_value";
            }
        };

        assertEquals("inspect_value", node.toString());
    }
}