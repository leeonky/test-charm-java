package com.github.leeonky.dal.ast.node;

import com.github.leeonky.dal.runtime.AssertionFailure;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.ExpectationFactory;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;

import java.util.regex.Pattern;

import static com.github.leeonky.dal.runtime.ExpressionException.illegalOperation;
import static com.github.leeonky.dal.runtime.ExpressionException.opt2;
import static java.lang.String.format;

public class RegexNode extends DALNode {
    private final Pattern pattern;

    public RegexNode(String regex) {
        pattern = Pattern.compile(regex, Pattern.DOTALL);
    }

    @Override
    public String inspect() {
        return format("/%s/", pattern.toString());
    }

    @Override
    protected ExpectationFactory toVerify(DALRuntimeContext context) {
        return (operator, actual) -> new ExpectationFactory.Expectation() {
            @Override
            public Data<?> matches() {
                String converted = opt2(() -> actual.convert(String.class).value());
                if (pattern.matcher(converted).matches())
                    return actual;
                throw new AssertionFailure(format("Expected to match: /%s/\nActual: <%s> converted from: %s", pattern,
                        converted, actual.dump()), getPositionBegin());
            }

            @Override
            public Data<?> equalTo() {
                String str = actual.cast(String.class)
                        .orElseThrow(() -> illegalOperation("Operator = before regex need a string input value"));
                if (pattern.matcher(str).matches())
                    return actual;
                throw new AssertionFailure(format("Expected to match: /%s/\nActual: <%s>", pattern, actual.value()), getPositionBegin());
            }

            @Override
            public ExpectationFactory.Type type() {
                return ExpectationFactory.Type.REGEX;
            }
        };
    }
}
