package com.github.leeonky.interpreter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestNode extends NodeBase<TestContext, TestNode> {
    private final Object content;

    public TestNode(Object content) {
        this.content = content;
    }

    public TestNode() {
        this(null);
    }

    public Object getContent() {
        return content;
    }
}
