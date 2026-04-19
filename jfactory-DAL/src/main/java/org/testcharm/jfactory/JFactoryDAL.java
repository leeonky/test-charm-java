package org.testcharm.jfactory;

import org.testcharm.dal.DAL;
import org.testcharm.dal.Evaluator;
import org.testcharm.dal.runtime.ProxyObject;
import org.testcharm.util.Collector;
import org.testcharm.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.testcharm.util.function.Consumers.noop;

public class JFactoryDAL {
    private final JFactory jFactory;
    private final DAL dal;

    public JFactoryDAL(JFactory jFactory, DAL dal) {
        this.jFactory = jFactory;
        this.dal = dal;
    }

    public JFactoryDAL(JFactory jFactory) {
        this(jFactory, DAL.dal("JFactory"));
    }

    public <T> T create(String traitsSpec, String expressions) {
        return create(traitsSpec, expressions, noop());
    }

    public <T> T create(String traitsSpec, String expressions, Consumer<JFactoryCollector> consumer) {
        JFactoryCollector collector = jFactory.collector(Object.class);
        String trim = expressions.trim();
        int codeOffset = 0;
        if (trim.startsWith("{") || trim.startsWith("|") || trim.startsWith("[")) {
            expressions = ":\n" + expressions;
            codeOffset = 2;
        }
        Evaluator.evaluateAll(expressions).codeOffset(codeOffset).by(dal).on(collector);
        return create(traitsSpec, consumer, collector);
    }

    public void createAll(String expressions) {
        Specs specs = new Specs();
        Evaluator.evaluateAll(expressions).by(dal).on(specs);
        specs.getCollectors().forEach(pair -> create(pair.getSecond().getTraits() + " " + pair.getFirst(), noop(), pair.getSecond()));
    }

    @SuppressWarnings("unchecked")
    private <T> T create(String traitsSpec, Consumer<JFactoryCollector> consumer, JFactoryCollector collector) {
        if (collector.type() == Collector.Type.LIST)
            return (T) collector.elements().values().stream().peek(consumer)
                    .map(sub -> sub.traitsSpec(asArray(traitsSpec)).build()).collect(toList());
        else {
            consumer.accept(collector);
            return (T) singletonList(collector.traitsSpec(asArray(traitsSpec)).build());
        }
    }

    private static String[] asArray(String traitsSpec) {
        return traitsSpec.trim().split(", |,| ");
    }

    class Specs implements ProxyObject {
        private final List<Pair<String, JFactoryCollectorWithoutTraits>> collectors = new ArrayList<>();

        @Override
        public Object getValue(Object property) {
            JFactoryCollectorWithoutTraits collector = new JFactoryCollectorWithoutTraits(jFactory);
            collectors.add(Pair.pair((String) property, collector));
            return collector;
        }

        public List<Pair<String, JFactoryCollectorWithoutTraits>> getCollectors() {
            return collectors;
        }
    }

    public static class JFactoryCollectorWithoutTraits extends JFactoryCollector {
        private String traits = "";

        JFactoryCollectorWithoutTraits(JFactory jFactory) {
            super(jFactory, Object.class);
        }

        public JFactoryCollectorWithoutTraits addTrait(String traits) {
            this.traits = traits;
            return this;
        }

        public String getTraits() {
            return traits;
        }
    }
}
