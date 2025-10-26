package com.github.leeonky.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import static com.github.leeonky.dal.Assertions.expect;
import static org.junit.jupiter.api.Assertions.assertSame;

class JavaExecutorTest {
    private final JavaExecutor executor = JavaExecutor.executor().resetAll();
    private final ClassLoader classLoader = URLClassLoader.newInstance(Sneaky.get(() -> new URL[]{new File("").toURI().toURL()}));

    @Nested
    class BaseApi {

        @Test
        void compile_and_get_class() {
            executor.addClass("public class Bean {}");

            expect(executor.classFor("Bean", classLoader))
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
}