package com.github.leeonky.dal.ast;

import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.dal.spec.Base;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class SchemaWhichExpressionTest {
    private static final SchemaExpression MATCHES_TYPE_EXPRESSION = new SchemaExpression(new ConstNode(1), singletonList(new SchemaNodeBak("Integer")), 0);
    RuntimeContextBuilder runtimeContextBuilder = new RuntimeContextBuilder();

    @Test
    void return_false_when_clause_is_false() {
        assertThat(MATCHES_TYPE_EXPRESSION.which(new ConstNode(false)).evaluate(runtimeContextBuilder.build(null)))
                .isEqualTo(false);
    }

    @Test
    void should_return_true_when_both_type_matches_and_clause_is_true() {
        assertThat(MATCHES_TYPE_EXPRESSION.which(new ConstNode(true)).evaluate(runtimeContextBuilder.build(null)))
                .isEqualTo(true);
    }

    @Test
    void should_wrapper_object_as_target_type() {
        DALNode schemaWhichExpression = new SchemaExpression(new ConstNode("http://www.baidu.com"),
                singletonList(new SchemaNodeBak("URL")), 0).omitWhich(Base.createPropertyNode(InputNode.INSTANCE, "protocol"));

        assertThat(schemaWhichExpression.evaluate(runtimeContextBuilder.build(null))).isEqualTo("http");
    }
}