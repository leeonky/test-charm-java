package com.github.leeonky.dal.ast.node;

import com.github.leeonky.dal.runtime.Data;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.github.leeonky.dal.ast.node.SortSymbolNode.Type.AZ;
import static com.github.leeonky.dal.ast.node.SortSymbolNode.Type.ZA;
import static com.github.leeonky.dal.compiler.Notations.*;

public class SortSymbolNode extends DALNode {
    private static final Map<String, Type> types = new HashMap<String, Type>() {{
        put(SEQUENCE_AZ.getLabel(), AZ);
        put(SEQUENCE_AZ_2.getLabel(), AZ);
        put(SEQUENCE_ZA.getLabel(), ZA);
        put(SEQUENCE_ZA_2.getLabel(), ZA);
    }};
    private final String label;
    private final Type type;

    public SortSymbolNode(String label) {
        this.label = label;
        type = types.get(label);
    }

    @Override
    public String inspect() {
        return label;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        AZ, ZA {
            @SuppressWarnings("unchecked")
            @Override
            Comparator<Data<?>> azOrZa(Function<Data<?>, Object> orderBy) {
                Comparator<Data<?>> comparator1 = Comparator.comparing(data -> (Comparable<Object>) orderBy.apply(data),
                        Comparator.nullsFirst(Comparator.reverseOrder()));
                return (d1, d2) -> {
//                    if (d2.isNull()) {
//                        return d1.isNull() ? 0 : 1;
//                    }
//                    if (d1.isNull()) {
//                        return -1;
//                    }
                    return comparator1.compare(d1, d2);
                };
            }
        };

        @SuppressWarnings("unchecked")
        Comparator<Data<?>> azOrZa(Function<Data<?>, Object> orderBy) {
            Comparator<Data<?>> comparator1 = Comparator.comparing(data -> (Comparable<Object>) orderBy.apply(data),
                    Comparator.nullsLast(Comparator.naturalOrder()));

            return (d1, d2) -> {
//                if (d1.isNull()) {
//                    return d2.isNull() ? 0 : 1;
//                }
//                if (d2.isNull()) {
//                    return -1;
//                }
                return comparator1.compare(d1, d2);
            };
        }
    }
}
