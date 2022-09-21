package com.github.leeonky.interpreter;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static com.github.leeonky.interpreter.IfThenFactory.when;
import static com.github.leeonky.interpreter.Notation.notation;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

public class Rules {
    public static <C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>, PA extends Parser<C, N, E, O, P, PA, MA, T>,
            MA extends Parser.Mandatory<C, N, E, O, P, PA, MA, T>, T, R, A> Function<Syntax<C, N, E, O, P, PA, MA, T, R, A>,
            Syntax<C, N, E, O, P, PA, MA, T, R, A>> endWith(Notation notation) {
        return syntax -> new Syntax.CompositeSyntax<C, N, E, O, P, PA, MA, T, R, A>(syntax) {
            @Override
            public void close(P procedure) {
                if (!procedure.getSourceCode().popWord(notation).isPresent())
                    throw procedure.getSourceCode().syntaxError(format("Should end with `%s`", notation.getLabel()), 0);
            }

            @Override
            public boolean isClose(P procedure) {
                return procedure.getSourceCode().startsWith(notation) || !procedure.getSourceCode().hasCode();
            }
        };
    }

    public static <C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>, PA extends Parser<C, N, E, O, P, PA, MA, T>,
            MA extends Parser.Mandatory<C, N, E, O, P, PA, MA, T>, T, R, A> Function<Syntax<C, N, E, O, P, PA, MA, T, R, A>,
            Syntax<C, N, E, O, P, PA, MA, T, R, A>> endBefore(Notation... notations) {
        return syntax -> new Syntax.CompositeSyntax<C, N, E, O, P, PA, MA, T, R, A>(syntax) {
            private boolean closed = false;

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
        };
    }

    public static <C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>, PA extends Parser<C, N, E, O, P, PA, MA, T>,
            MA extends Parser.Mandatory<C, N, E, O, P, PA, MA, T>, T, R, A> Function<Syntax<C, N, E, O, P, PA, MA, T, R, A>,
            Syntax<C, N, E, O, P, PA, MA, T, R, A>> endWith(String closing) {
        return syntax -> new Syntax.CompositeSyntax<C, N, E, O, P, PA, MA, T, R, A>(syntax.and(Rules.endWith(notation(closing)))) {
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
        return syntax -> new Syntax.CompositeSyntax<C, N, E, O, P, PA, MA, T, R, A>(syntax) {
            private boolean isClose = false;

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
        };
    }

    public static <C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>, PA extends Parser<C, N, E, O, P, PA, MA, T>,
            MA extends Parser.Mandatory<C, N, E, O, P, PA, MA, T>, T, R, A> Function<Syntax<C, N, E, O, P, PA, MA, T, R, A>,
            Syntax<C, N, E, O, P, PA, MA, T, R, A>> splitBy(Notation notation) {
        return syntax -> new Syntax.CompositeSyntax<C, N, E, O, P, PA, MA, T, R, A>(syntax) {
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
        return syntax -> new Syntax.CompositeSyntax<C, N, E, O, P, PA, MA, T, R, A>(syntax.and(Rules.endWithLine())) {
            @Override
            public void close(P procedure) {
            }
        };
    }

    public static <C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>, PA extends Parser<C, N, E, O, P, PA, MA, T>,
            MA extends Parser.Mandatory<C, N, E, O, P, PA, MA, T>, T, R, A> Function<Syntax<C, N, E, O, P, PA, MA, T, R, A>,
            Syntax<C, N, E, O, P, PA, MA, T, R, A>> optionalSplitBy(Notation splitter) {
        return syntax -> new Syntax.CompositeSyntax<C, N, E, O, P, PA, MA, T, R, A>(syntax) {
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
        return syntax -> new Syntax.CompositeSyntax<C, N, E, O, P, PA, MA, T, R, A>(syntax) {
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
        return syntax -> new Syntax.CompositeSyntax<C, N, E, O, P, PA, MA, T, NodeParser<C, N, E, O, P>, List<T>>(
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
            NodeParser<C, N, E, O, P>, A>, Syntax<C, N, E, O, P, PA, MA, T, NodeParser<C, N, E, O, P>, A>> enabledBefore(Notation notation) {
        return syntax -> new Syntax.CompositeSyntax<C, N, E, O, P, PA, MA, T, NodeParser<C, N, E, O, P>, A>(syntax) {

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
        return syntax -> new Syntax.CompositeSyntax<C, N, E, O, P, PA, MA, T, R, A>(syntax) {
            private boolean isClose = false;

            @Override
            public boolean isClose(P procedure) {
                isClose = procedure.getSourceCode().isEndOfLine() || !procedure.getSourceCode().hasCode();
                if (isClose) {
                    if (procedure.getSourceCode().hasCode())
                        procedure.getSourceCode().popChar(Collections.emptyMap());
                } else {
                    String code = procedure.getSourceCode().codeBefore(splitter);
                    isClose = code.contains("\r") || code.contains("\n");
                }
                return isClose;
            }

            @Override
            public void close(P procedure) {
                if (!isClose)
                    throw procedure.getSourceCode().syntaxError("unexpected token", 0);
            }
        };
    }
}
