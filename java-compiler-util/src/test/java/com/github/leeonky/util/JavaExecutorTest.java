package com.github.leeonky.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.github.leeonky.dal.Assertions.expect;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class JavaExecutorTest {
    private final JavaExecutor executor = JavaExecutor.executor().resetAll();

    @Nested
    class BaseApi {

        @Test
        void compile_and_get_class() {
            executor.addClass("public class Bean {}");

            expect(executor.classOf("Bean"))
                    .should("simpleName= Bean");
        }

        @Test
        void evaluate_value() {
            executor.main().returnExpression("10");

            expect(executor.main().evaluate()).isEqualTo(10);
        }

        @Test
        void add_declaration() {
            executor.main().addDeclarations("int i=100");

            executor.main().returnExpression("i+1");

            expect(executor.main().evaluate()).isEqualTo(101);
        }

        @Test
        void more_than_one_declaration() {
            executor.main().addDeclarations("int x=100, y=200");

            executor.main().returnExpression("x+y");

            expect(executor.main().evaluate()).isEqualTo(300);
        }

        @Test
        void add_register() {
            executor.main().addDeclarations("int i=100");

            executor.main().addRegisters("i++");

            executor.main().returnExpression("i");

            expect(executor.main().evaluate()).isEqualTo(101);
        }

        @Test
        void more_than_one_register() {
            executor.main().addDeclarations("int i=100");

            executor.main().addRegisters("i++");
            executor.main().addRegisters("i++");

            executor.main().returnExpression("i");

            expect(executor.main().evaluate()).isEqualTo(102);
        }

        @Test
        void add_args_to_executor() {
            executor.main().addArg("var", 100);

            executor.main().returnExpression("args.get(\"var\")");

            expect(executor.main().evaluate()).isEqualTo(100);
        }
    }

    @Nested
    class EvaluateTest {

        @Nested
        class ReUseEvaluator {

            @Test
            void should_use_same_executor_instance_when_executor_code_not_changed() {
                executor.main().addDeclarations("int i=100");

                executor.main().returnExpression("i++");

                expect(executor.main().evaluate()).isEqualTo(100);
                expect(executor.main().evaluate()).isEqualTo(101);
            }

            @Test
            void should_not_recompile_evaluator_when_set_the_same_return_expression() {
                executor.main().addDeclarations("int i=100");

                expect(executor.main().returnExpression("i++").evaluate()).isEqualTo(100);
                expect(executor.main().returnExpression("i++").evaluate()).isEqualTo(101);
            }
        }

        @Nested
        class ReCompileEvaluator {

            @Test
            void should_recompile_evaluator_when_add_declaration() {
                executor.main().addDeclarations("int i=100");

                expect(executor.main().returnExpression("i++").evaluate()).isEqualTo(100);

                executor.main().addDeclarations("int j=100");

                expect(executor.main().returnExpression("i++").evaluate()).isEqualTo(100);
            }

            @Test
            void should_recompile_evaluator_when_add_register() {
                executor.main().addDeclarations("int i=100");

                expect(executor.main().returnExpression("i++").evaluate()).isEqualTo(100);

                executor.main().addRegisters("int any=0");

                expect(executor.main().returnExpression("i++").evaluate()).isEqualTo(100);
            }

            @Test
            void should_recompile_evaluator_when_change_return_expression() {
                executor.main().addDeclarations("int i=100");

                expect(executor.main().returnExpression("i++").evaluate()).isEqualTo(100);
                expect(executor.main().returnExpression("(i++)").evaluate()).isEqualTo(100);
            }

            @Test
            void should_use_the_same_declaration_instance_during_evaluations() {
                executor.main().addDeclarations("java.util.List<String> list = new java.util.ArrayList<>()");

                Object list1 = executor.main().returnExpression("list").evaluate();
                Object list2 = executor.main().returnExpression("(java.util.List)list").evaluate();

                assertSame(list1, list2);
            }

            @Test
            void should_not_register_original_twice_when_recompile() {
                executor.main().addDeclarations("int[] ints = new int[]{0};");

                executor.main().addRegisters("ints[0]++");

                executor.main().returnExpression("ints[0]");

                expect(executor.main().evaluate()).isEqualTo(1);

                executor.main().addRegisters("ints[0]++");

                executor.main().returnExpression("ints[0]");

                expect(executor.main().evaluate()).isEqualTo(2);
            }
        }
    }

    @Nested
    class CompileCache {

        @Test
        void new_class() {
            JavaCompiler realCompiler = new JavaCompiler("src.test.generate.t", 0);
            JavaCompiler spyCompiler = Mockito.spy(realCompiler);

            JavaExecutor executor = new JavaExecutor(spyCompiler);

            executor.addClass("public class Foo {}");
            expect(executor.classOf("Foo")).should("simpleName= Foo");
            verify(spyCompiler, times(1)).compile(anyCollection());
        }

        @Test
        void should_not_recompile_same_code() {
            JavaCompiler realCompiler = new JavaCompiler("src.test.generate.t", 0);
            JavaCompiler spyCompiler = Mockito.spy(realCompiler);

            JavaExecutor executor = new JavaExecutor(spyCompiler);

            executor.addClass("public class Foo {}");
            executor.classOf("Foo");

            executor.addClass("public class Foo {}");
            executor.classOf("Foo");

            verify(spyCompiler, times(1)).compile(anyCollection());
        }

        @Test
        void should_delete_exist_same_name_class_file_after_add_different_content_class() {
            JavaCompiler realCompiler = new JavaCompiler("src.test.generate.t", 0);
            JavaCompiler spyCompiler = Mockito.spy(realCompiler);

            JavaExecutor executor = new JavaExecutor(spyCompiler);

            executor.addClass("public class Foo {}");
            executor.addClass("public class Bar {}");
            executor.classOf("Foo");

            executor.addClass("public class Foo {int any;}");
            expect(realCompiler.getLocation()).should("name[]= ['Bar.class']");
            executor.classOf("Foo");

            verify(spyCompiler, times(2)).compile(anyCollection());
        }

        @Test
        void replace_uncompiled_class() throws InstantiationException, IllegalAccessException {
            JavaCompiler realCompiler = new JavaCompiler("src.test.generate.t", 0);
            JavaCompiler spyCompiler = Mockito.spy(realCompiler);

            JavaExecutor executor = new JavaExecutor(spyCompiler);

            executor.addClass("public class Foo {}");
            executor.addClass("public class Foo { public int i= 100;}");
            expect(executor.classOf("Foo").newInstance()).should("i= 100");
            verify(spyCompiler, times(1)).compile(anyCollection());
        }
    }
}