package com.github.leeonky.interpreter;

import com.github.leeonky.interpreter.Syntax.CompositeSyntax;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static com.github.leeonky.interpreter.Notation.notation;
import static com.github.leeonky.util.function.When.when;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

public class Rules {
    public static <C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>, PA extends Parser<C, N, E, O, P, PA, MA, T>,
            MA extends Parser.Mandatory<C, N, E, O, P, PA, MA, T>, T, R, A> Function<Syntax<C, N, E, O, P, PA, MA, T, R, A>,
            Syntax<C, N, E, O, P, PA, MA, T, R, A>> endWith(Notation notation) {
        return syntax -> new EndWith<>(syntax, notation);
    }

    public static <C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>, PA extends Parser<C, N, E, O, P, PA, MA, T>,
            MA extends Parser.Mandatory<C, N, E, O, P, PA, MA, T>, T, R, A> Function<Syntax<C, N, E, O, P, PA, MA, T, R, A>,
            Syntax<C, N, E, O, P, PA, MA, T, R, A>> endBefore(Notation... notations) {
        return syntax -> new EndBefore<>(syntax, notations);
    }

    public static <C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>, PA extends Parser<C, N, E, O, P, PA, MA, T>,
            MA extends Parser.Mandatory<C, N, E, O, P, PA, MA, T>, T, R, A> Function<Syntax<C, N, E, O, P, PA, MA, T, R, A>,
            Syntax<C, N, E, O, P, PA, MA, T, R, A>> endWith(String closing) {
        return syntax -> new CompositeSyntax<C, N, E, O, P, PA, MA, T, R, A>(syntax.and(Rules.endWith(notation(closing)))) {
            @Override
            public boolean isClose(P procedure) {
                return !procedure.getSourceCode().hasCode() || procedure.getSourceCode().startsWith(closing);
            }
        };
    }

    public static <C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>, PA extends Parser<C, N, E, O, P, PA, MA, T>,
            MA extends Parser.Mandatory<C, N, E, O, P, PA, MA, T>, T, R, A> Function<Syntax<C, N, E, O, P, PA, MA, T, R, A>,
            Syntax<C, N, E, O, P, PA, MA, T, R, A>> endWithLine() {
        return EndWithLine::new;
    }

    public static <C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>, PA extends Parser<C, N, E, O, P, PA, MA, T>,
            MA extends Parser.Mandatory<C, N, E, O, P, PA, MA, T>, T, R, A> Function<Syntax<C, N, E, O, P, PA, MA, T, R, A>,
            Syntax<C, N, E, O, P, PA, MA, T, R, A>> splitBy(Notation notation) {
        return syntax -> new CompositeSyntax<C, N, E, O, P, PA, MA, T, R, A>(syntax) {
            @Override
            public boolean isSplitter(P procedure) {
                return procedure.getSourceCode().popWord(notation).isPresent();
            }
        };
    }

    public static <C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>, PA extends Parser<C, N, E, O, P, PA, MA, T>,
            MA extends Parser.Mandatory<C, N, E, O, P, PA, MA, T>, T, R, A> Function<Syntax<C, N, E, O, P, PA, MA, T, R, A>,
            Syntax<C, N, E, O, P, PA, MA, T, R, A>> endWithOptionalLine() {
        return syntax -> new CompositeSyntax<C, N, E, O, P, PA, MA, T, R, A>(syntax.and(Rules.endWithLine())) {
            @Override
            public void close(P procedure) {
            }
        };
    }

    public static <C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>, PA extends Parser<C, N, E, O, P, PA, MA, T>,
            MA extends Parser.Mandatory<C, N, E, O, P, PA, MA, T>, T, R, A> Function<Syntax<C, N, E, O, P, PA, MA, T, R, A>,
            Syntax<C, N, E, O, P, PA, MA, T, R, A>> optionalSplitBy(Notation splitter) {
        return syntax -> new CompositeSyntax<C, N, E, O, P, PA, MA, T, R, A>(syntax) {
            @Override
            public boolean isSplitter(P procedure) {
                procedure.getSourceCode().popWord(splitter);
                return true;
            }
        };
    }

    public static <C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>, PA extends Parser<C, N, E, O, P, PA, MA, T>,
            MA extends Parser.Mandatory<C, N, E, O, P, PA, MA, T>, T, R, A> Function<Syntax<C, N, E, O, P, PA, MA, T, R, A>,
            Syntax<C, N, E, O, P, PA, MA, T, R, A>> mandatorySplitBy(Notation splitter) {
        return syntax -> new CompositeSyntax<C, N, E, O, P, PA, MA, T, R, A>(syntax) {
            @Override
            public boolean isSplitter(P procedure) {
                if (procedure.getSourceCode().popWord(splitter).isPresent())
                    return true;
                throw procedure.getSourceCode().syntaxError(format("Should end with `%s`", splitter.getLabel()), 0);
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>, PA extends Parser<C, N, E, O, P, PA, MA, T>,
            MA extends Parser.Mandatory<C, N, E, O, P, PA, MA, T>, T, R, A> Function<Syntax<C, N, E, O, P, PA, MA, T, R, A>,
            Syntax<C, N, E, O, P, PA, MA, T, NodeParser<C, N, E, O, P>, List<T>>> atLeast(int size) {
        return syntax -> new CompositeSyntax<C, N, E, O, P, PA, MA, T, NodeParser<C, N, E, O, P>, List<T>>(
                (Syntax<C, N, E, O, P, PA, MA, T, NodeParser<C, N, E, O, P>, List<T>>) syntax) {
            @Override
            protected NodeParser<C, N, E, O, P> parse(Syntax<C, N, E, O, P, PA, MA, T, NodeParser<C, N, E, O, P>, List<T>> syntax,
                                                      Function<List<T>, N> factory) {
                return procedure -> procedure.getSourceCode().tryFetch(() -> {
                    List<T> list = parser.apply(procedure, syntax);
                    return when(list.size() >= size).optional(() -> factory.apply(list));
                });
            }
        };
    }

    public static <C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>, PA extends Parser<C, N, E, O, P, PA, MA, T>,
            MA extends Parser.Mandatory<C, N, E, O, P, PA, MA, T>, T, A> Function<Syntax<C, N, E, O, P, PA, MA, T,
            NodeParser<C, N, E, O, P>, A>, Syntax<C, N, E, O, P, PA, MA, T, NodeParser<C, N, E, O, P>, A>> enabledBefore(
            Notation notation) {
        return syntax -> new CompositeSyntax<C, N, E, O, P, PA, MA, T, NodeParser<C, N, E, O, P>, A>(syntax) {

            @Override
            protected NodeParser<C, N, E, O, P> parse(Syntax<C, N, E, O, P, PA, MA, T, NodeParser<C, N, E, O, P>, A> syntax,
                                                      Function<A, N> factory) {
                NodeParser<C, N, E, O, P> nodeParser = super.parse(syntax, factory);
                return procedure -> procedure.getSourceCode().tryFetch(() -> nodeParser.parse(procedure).map(node ->
                        procedure.getSourceCode().startsWith(notation) ? node : null));
            }
        };
    }

    public static <C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>, PA extends Parser<C, N, E, O, P, PA, MA, T>,
            MA extends Parser.Mandatory<C, N, E, O, P, PA, MA, T>, T, R, A> Function<Syntax<C, N, E, O, P, PA, MA, T, R, A>,
            Syntax<C, N, E, O, P, PA, MA, T, R, A>> endOfRow(Notation splitter) {
        return syntax -> new EndOrRow<>(syntax, splitter);
    }

    private static class EndOrRow<C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>, PA extends Parser<C, N, E, O, P, PA, MA, T>,
            MA extends Parser.Mandatory<C, N, E, O, P, PA, MA, T>, T, R, A> extends CompositeSyntax<C, N, E, O, P, PA, MA, T, R, A> {
        private final Notation splitter;
        private boolean isClose;

        public EndOrRow(Syntax<C, N, E, O, P, PA, MA, T, R, A> syntax, Notation splitter) {
            super(syntax);
            this.splitter = splitter;
            isClose = false;
        }

        @Override
        public boolean isClose(P procedure) {
            return isClose = endOfLineOrNoCode(procedure.getSourceCode())
                    || hasNewLineBeforeSplitter(procedure.getSourceCode());
        }

        private boolean hasNewLineBeforeSplitter(SourceCode sourceCode) {
            String code = sourceCode.codeBefore(splitter);
            return code.contains("\r") || code.contains("\n");
        }

        private boolean endOfLineOrNoCode(SourceCode sourceCode) {
            if (sourceCode.isEndOfLine() || !sourceCode.hasCode()) {
                if (sourceCode.hasCode())
                    sourceCode.popChar(Collections.emptyMap());
                return true;
            }
            return false;
        }

        @Override
        public void close(P procedure) {
            if (!isClose)
                throw procedure.getSourceCode().syntaxError("unexpected token", 0);
        }
    }

    private static class EndWithLine<C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>, PA extends Parser<C, N, E, O, P, PA, MA, T>,
            MA extends Parser.Mandatory<C, N, E, O, P, PA, MA, T>, T, R, A> extends CompositeSyntax<C, N, E, O, P, PA, MA, T, R, A> {
        private boolean isClose;

        public EndWithLine(Syntax<C, N, E, O, P, PA, MA, T, R, A> syntax) {
            super(syntax);
            isClose = false;
        }

        @Override
        public boolean isClose(P procedure) {
            isClose = procedure.getSourceCode().isEndOfLine();
            if (isClose && procedure.getSourceCode().hasCode())
                procedure.getSourceCode().popChar(Collections.emptyMap());
            return isClose;
        }

        @Override
        public void close(P procedure) {
            if (!isClose)
                throw procedure.getSourceCode().syntaxError("unexpected token", 0);
        }
    }

    private static class EndWith<C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>, PA extends Parser<C, N, E, O, P, PA, MA, T>,
            MA extends Parser.Mandatory<C, N, E, O, P, PA, MA, T>, T, R, A> extends CompositeSyntax<C, N, E, O, P, PA, MA, T, R, A> {
        private final Notation notation;

        public EndWith(Syntax<C, N, E, O, P, PA, MA, T, R, A> syntax, Notation notation) {
            super(syntax);
            this.notation = notation;
        }

        @Override
        public void close(P procedure) {
            if (!procedure.getSourceCode().popWord(notation).isPresent())
                throw procedure.getSourceCode().syntaxError(format("Should end with `%s`", notation.getLabel()), 0);
        }

        @Override
        public boolean isClose(P procedure) {
            return procedure.getSourceCode().startsWith(notation) || !procedure.getSourceCode().hasCode();
        }
    }

    private static class EndBefore<C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>, PA extends Parser<C, N, E, O, P, PA, MA, T>,
            MA extends Parser.Mandatory<C, N, E, O, P, PA, MA, T>, T, R, A> extends CompositeSyntax<C, N, E, O, P, PA, MA, T, R, A> {
        private final Notation[] notations;
        private boolean closed;

        public EndBefore(Syntax<C, N, E, O, P, PA, MA, T, R, A> syntax, Notation... notations) {
            super(syntax);
            this.notations = notations;
            closed = false;
        }

        @Override
        public void close(P procedure) {
            if (!closed)
                throw procedure.getSourceCode().syntaxError("Should end with " +
                        stream(notations).map(Notation::getLabel).collect(joining("`", "`", "` or `")), 0);
        }

        @Override
        public boolean isClose(P procedure) {
            return closed = stream(notations).anyMatch(procedure.getSourceCode()::startsWith);
        }
    }
}
