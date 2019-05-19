package com.github.leeonky.dal.ast.opt;

import com.github.leeonky.dal.CompilingContext;
import com.github.leeonky.dal.DALCompiler;
import com.github.leeonky.dal.ast.Node;

public abstract class Operator {
    private final String code;
    private final boolean isKeyword;

    public Operator(String code) {
        this.code = code;
        isKeyword = false;
    }

    public Operator(String code, boolean isKeyword) {
        this.code = code;
        this.isKeyword = isKeyword;
    }

    public abstract Object calculate(CompilingContext context, Node node1, Node node2);

    public boolean isMatch(String content) {
        return content.startsWith(code) && (!isKeyword || DALCompiler.isSpliter(content, length()));
    }

    public int length() {
        return code.length();
    }
}
