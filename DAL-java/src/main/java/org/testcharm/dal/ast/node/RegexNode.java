package org.testcharm.dal.ast.node;

import org.testcharm.dal.runtime.AssertionFailure;
import org.testcharm.dal.runtime.Data;
import org.testcharm.dal.runtime.ExpectationFactory;
import org.testcharm.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;

import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.testcharm.dal.runtime.ExpressionException.illegalOperation;
import static org.testcharm.dal.runtime.ExpressionException.opt2;

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
//
//            @Override
//            public ExpectationFactory.Type type() {
//                return ExpectationFactory.Type.REGEX;
//            }
        };
    }
}
