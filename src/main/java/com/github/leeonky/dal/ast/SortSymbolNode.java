package com.github.leeonky.dal.ast;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static com.github.leeonky.dal.ast.SortSymbolNode.Type.AZ;
import static com.github.leeonky.dal.ast.SortSymbolNode.Type.ZA;
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
            @Override
            Comparator<Object> azOrZa(Comparator<Object> comparator) {
                return comparator.reversed();
            }
        };

        Comparator<Object> azOrZa(Comparator<Object> comparator) {
            return comparator;
        }
    }
}
