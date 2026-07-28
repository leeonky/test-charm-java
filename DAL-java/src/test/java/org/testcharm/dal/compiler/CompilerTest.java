package org.testcharm.dal.compiler;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcharm.dal.DAL;
import org.testcharm.dal.runtime.Result;
import org.testcharm.dal.runtime.RuntimeContextBuilder;
import org.testcharm.dal.runtime.UserLiteralRule;
import org.testcharm.interpreter.SourceCode;
import org.testcharm.util.NumberParser;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

class CompilerTest {

    @Nested
    class ExpressionRelaxString {
        private Compiler compiler = new Compiler();
        private RuntimeContextBuilder.DALRuntimeContext runtimeContext = new RuntimeContextBuilder().build(null);

        @Test
        void relax_string_end_with_chars() {
            Set<Character> DELIMITER = new HashSet<>(asList('\r'));

            DELIMITER.forEach(c -> relaxStringShouldBe(String.format("hello%cworld", c), "hello"));

            relaxStringShouldBe("hello", "hello");
        }

        private void relaxStringShouldBe(String code, String expected) {
            assertThat(compiler.EXPRESSION_RELAX_STRING.parse(new DALProcedure(new SourceCode(code, emptyList()),
                    runtimeContext)).evaluate(runtimeContext)).isEqualTo(expected);
        }
    }

    @Nested
    class ObjectScopeRelaxString {
        private Compiler compiler = new Compiler();
        private RuntimeContextBuilder.DALRuntimeContext runtimeContext = new RuntimeContextBuilder().build(null);

        @Test
        void relax_string_end_with_chars() {
            Set<Character> DELIMITER = new HashSet<>(asList('\r'));

            DELIMITER.forEach(c -> relaxStringShouldBe(String.format("hello%cworld", c), "hello"));

            relaxStringShouldBe("hello", "hello");
        }

        private void relaxStringShouldBe(String code, String expected) {
            assertThat(compiler.OBJECT_SCOPE_RELAX_STRING.parse(new DALProcedure(new SourceCode(code, emptyList()),
                    runtimeContext)).evaluate(runtimeContext)).isEqualTo(expected);
        }
    }

    @Nested
    class ListScopeRelaxString {
        private Compiler compiler = new Compiler();
        private RuntimeContextBuilder.DALRuntimeContext runtimeContext = new RuntimeContextBuilder().build(null);

        @Test
        void relax_string_end_with_chars() {
            Set<Character> DELIMITER = new HashSet<>(asList('\r'));

            DELIMITER.forEach(c -> relaxStringShouldBe(String.format("hello%cworld", c), "hello"));

            relaxStringShouldBe("hello", "hello");
        }

        private void relaxStringShouldBe(String code, String expected) {
            assertThat(compiler.LIST_SCOPE_RELAX_STRING.parse(new DALProcedure(new SourceCode(code, emptyList()),
                    runtimeContext)).evaluate(runtimeContext)).isEqualTo(expected);
        }
    }

    @Nested
    class UserDefinedLiteral {

        @Test
        void redefine_hex_number() {
            DAL dal = new DAL();
            dal.getRuntimeContextBuilder().registerUserDefinedLiterals(new UserLiteralRule() {

                @Override
                public Result compile(String token) {
                    if (token.startsWith("0x") || token.startsWith("0X")) {
                        return Result.of(new NumberParser().parseNumber(token).toString());
                    }
                    return Result.empty();
                }
            });

            assertThat((Object) dal.evaluate(null, "0x1")).isEqualTo("1");
        }
    }
}