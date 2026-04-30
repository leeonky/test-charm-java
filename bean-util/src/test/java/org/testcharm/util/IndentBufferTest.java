package org.testcharm.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IndentBufferTest {

    private static final int NO_LIMIT = Integer.MAX_VALUE;

    @Nested
    class Append {

        @Test
        void write_text_directly_to_content() {
            IndentBuffer buffer = IndentBuffer.create(NO_LIMIT);

            buffer.append("hello");

            assertEquals("hello", buffer.content());
        }

        @Test
        void multiple_appends_concatenate_without_separator() {
            IndentBuffer buffer = IndentBuffer.create(NO_LIMIT);

            buffer.append("foo").append("bar").append("baz");

            assertEquals("foobarbaz", buffer.content());
        }
    }

    @Nested
    class PendingContent {

        @Test
        void pending_text_is_written_before_next_append() {
            IndentBuffer buffer = IndentBuffer.create(NO_LIMIT);

            buffer.append("a").defer(",").append("b");

            assertEquals("a,b", buffer.content());
        }

        @Test
        void pending_text_is_not_written_without_subsequent_append() {
            IndentBuffer buffer = IndentBuffer.create(NO_LIMIT);

            buffer.append("a").defer(",");

            assertEquals("a", buffer.content());
        }

        @Test
        void multiple_pending_calls_accumulate_in_order() {
            IndentBuffer buffer = IndentBuffer.create(NO_LIMIT);

            buffer.append("a").defer("-").defer(">").append("b");

            assertEquals("a->b", buffer.content());
        }

        @Test
        void multiple_pending_text_is_not_written_without_subsequent_append() {
            IndentBuffer buffer = IndentBuffer.create(NO_LIMIT);

            buffer.append("a").defer("-").defer(">");

            assertEquals("a", buffer.content());
        }
    }

    @Nested
    class NewLine {

        @Test
        void inserts_bare_newline_at_root_level() {
            IndentBuffer buffer = IndentBuffer.create(NO_LIMIT);

            buffer.append("first").newLine().append("second");

            assertEquals(
                    "first\n" +
                            "second",
                    buffer.content());
        }

        @Test
        void inserts_newline_with_four_spaces_at_indent_level_1() {
            IndentBuffer buffer = IndentBuffer.create(NO_LIMIT);
            IndentBuffer indented = buffer.indent();

            indented.newLine().append("child");

            assertEquals(
                    "\n" +
                            "    child",
                    buffer.content());
        }

        @Test
        void inserts_newline_with_eight_spaces_at_indent_level_2() {
            IndentBuffer buffer = IndentBuffer.create(NO_LIMIT);
            IndentBuffer level2 = buffer.indent().indent();

            level2.newLine().append("deep");

            assertEquals(
                    "\n" +
                            "        deep",
                    buffer.content());
        }
    }

    @Nested
    class OptionalNewLine {

        @Test
        void adds_newline_when_content_was_written_after_last_append() {
            IndentBuffer buffer = IndentBuffer.create(NO_LIMIT);
            IndentBuffer indented = buffer.append("{").indent();

            indented.newLine().append("field: value");
            buffer.optionalNewLine().append("}");

            assertEquals(
                    "{\n" +
                            "    field: value\n" +
                            "}",
                    buffer.content());
        }

        @Test
        void skips_newline_when_no_content_was_written_after_last_append() {
            IndentBuffer buffer = IndentBuffer.create(NO_LIMIT);
            IndentBuffer indented = buffer.append("{").indent();

            // nothing written to indented
            buffer.optionalNewLine().append("}");

            assertEquals("{}", buffer.content());
        }
    }

    @Nested
    class SubBuffer {

        @Nested
        class Fork {

            @Test
            void fork_keeps_same_indent_level() {
                IndentBuffer buffer = IndentBuffer.create(NO_LIMIT);
                IndentBuffer sub = buffer.fork();

                sub.newLine().append("same level");

                assertEquals(
                        "\n" +
                                "same level",
                        buffer.content());
            }

            @Test
            void fork_shares_output_with_parent() {
                IndentBuffer buffer = IndentBuffer.create(NO_LIMIT);

                buffer.append("a").fork().append("b");
                buffer.append("c");

                assertEquals("abc", buffer.content());
            }

            @Test
            void parent_pending_transferred_to_fork_and_written_before_fork_content() {
                IndentBuffer buffer = IndentBuffer.create(NO_LIMIT);

                buffer.append("key").defer(": ").fork().append("value");

                assertEquals("key: value", buffer.content());
            }

            @Test
            void parent_pending_cleared_after_fork() {
                IndentBuffer buffer = IndentBuffer.create(NO_LIMIT);

                buffer.append("a").defer("-").fork().append("b");
                buffer.append("c");   // parent pending was already transferred, nothing between b and c

                assertEquals("a-bc", buffer.content());
            }
        }

        @Nested
        class Indent {

            @Test
            void increases_indent_level_by_one() {
                IndentBuffer buffer = IndentBuffer.create(NO_LIMIT);
                IndentBuffer indented = buffer.append("parent").indent();

                indented.newLine().append("child");

                assertEquals(
                        "parent\n" +
                                "    child",
                        buffer.content());
            }

            @Test
            void each_indent_call_adds_another_level() {
                IndentBuffer buffer = IndentBuffer.create(NO_LIMIT);
                IndentBuffer level1 = buffer.append("root").indent();
                IndentBuffer level2 = level1.newLine().append("level1").indent();

                level2.newLine().append("level2");

                assertEquals(
                        "root\n" +
                                "    level1\n" +
                                "        level2",
                        buffer.content());
            }

            @Test
            void fork_shares_output_with_parent() {
                IndentBuffer buffer = IndentBuffer.create(NO_LIMIT);
                IndentBuffer indented = buffer.append("{").indent();

                indented.newLine().append("item");
                buffer.optionalNewLine().append("}");

                assertEquals(
                        "{\n" +
                                "    item\n" +
                                "}",
                        buffer.content());
            }

            @Test
            void parent_pending_is_written_at_the_start_of_indented_content() {
                IndentBuffer buffer = IndentBuffer.create(NO_LIMIT);

                buffer.append("header").defer(" ").indent().newLine().append("body");

                assertEquals(
                        "header \n" +
                                "    body",
                        buffer.content());
            }
        }
    }

    @Nested
    class LineCountLimit {

        @Test
        void truncates_output_when_max_line_count_is_reached() {
            IndentBuffer buffer = IndentBuffer.create(2);

            buffer.append("line1").newLine()
                    .append("line2").newLine()
                    .append("line3");

            assertEquals(
                    "line1\n" +
                            "line2\n" +
                            "...",
                    buffer.content());
        }

        @Test
        void content_written_after_truncation_is_ignored() {
            IndentBuffer buffer = IndentBuffer.create(1);

            buffer.append("line1").newLine()
                    .append("ignored1").newLine()
                    .append("ignored2");

            assertEquals(
                    "line1\n" +
                            "...",
                    buffer.content());
        }

        @Test
        void truncation_applies_across_sub_buffers_sharing_the_same_content() {
            IndentBuffer buffer = IndentBuffer.create(2);
            IndentBuffer indented = buffer.append("root").indent();

            indented.newLine().append("child1");
            indented.newLine().append("child2 - beyond limit");

            assertEquals(
                    "root\n" +
                            "    child1\n" +
                            "...",
                    buffer.content());
        }
    }
}
