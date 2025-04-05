package com.github.leeonky.dal.runtime;

import com.github.leeonky.dal.ast.node.DALNode;
import com.github.leeonky.dal.ast.opt.DALOperator;
import com.github.leeonky.dal.format.Formatter;
import com.github.leeonky.dal.runtime.checker.Checker;
import com.github.leeonky.dal.runtime.checker.CheckerSet;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumperFactory;
import com.github.leeonky.dal.runtime.schema.Expect;
import com.github.leeonky.dal.type.ExtensionName;
import com.github.leeonky.dal.type.InputCode;
import com.github.leeonky.dal.type.Schema;
import com.github.leeonky.interpreter.RuntimeContext;
import com.github.leeonky.interpreter.SyntaxException;
import com.github.leeonky.util.*;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.leeonky.dal.runtime.CurryingMethod.createCurryingMethod;
import static com.github.leeonky.dal.runtime.ExpressionException.illegalOp2;
import static com.github.leeonky.dal.runtime.ExpressionException.illegalOperation;
import static com.github.leeonky.dal.runtime.schema.Actual.actual;
import static com.github.leeonky.dal.runtime.schema.Verification.expect;
import static com.github.leeonky.util.Classes.getClassName;
import static com.github.leeonky.util.Classes.named;
import static com.github.leeonky.util.CollectionHelper.toStream;
import static java.lang.String.format;
import static java.lang.reflect.Modifier.STATIC;
import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static java.util.Optional.of;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class RuntimeContextBuilder {
    private final ClassKeyMap<PropertyAccessor<Object>> propertyAccessors = new ClassKeyMap<>();
    private final ClassKeyMap<DALCollectionFactory<Object, Object>> dALCollectionFactories = new ClassKeyMap<>();
    private final ClassKeyMap<Function<Object, Object>> objectImplicitMapper = new ClassKeyMap<>();
    private final ClassKeyMap<Function<Object, Comparable<?>>> customSorters = new ClassKeyMap<>();
    private final Map<String, ConstructorViaSchema> valueConstructors = new LinkedHashMap<>();
    private final Map<String, BeanClass<?>> schemas = new HashMap<>();
    private final Set<Method> extensionMethods = new HashSet<>();
    private final Map<Object, Function<MetaData, Object>> metaProperties = new HashMap<>();
    private final ClassKeyMap<Function<RemarkData, Object>> remarks = new ClassKeyMap<>();
    private final ClassKeyMap<Function<RuntimeData, Object>> exclamations = new ClassKeyMap<>();
    private final List<UserLiteralRule> userDefinedLiterals = new ArrayList<>();
    private final NumberType numberType = new NumberType();
    private final Map<Method, BiFunction<Object, List<Object>, List<Object>>> curryingMethodArgRanges = new HashMap<>();
    private final Map<String, TextFormatter<?, ?>> textFormatterMap = new LinkedHashMap<>();
    private final Map<Operators, LinkedList<Operation>> operations = new HashMap<>();
    private Converter converter = Converter.getInstance();
    private final ClassKeyMap<DumperFactory> dumperFactories = new ClassKeyMap<>();
    private final CheckerSet checkerSetForMatching = new CheckerSet(CheckerSet::defaultMatching);
    private final CheckerSet checkerSetForEqualing = new CheckerSet(CheckerSet::defaultEqualing);
    private int maxDumpingLineSize = 2000;
    private int maxDumpingObjectSize = 255;
    private ErrorHook errorHook = (i, code, e) -> false;
    private final Map<Class<?>, Map<Object, Function<MetaData, Object>>> localMetaProperties
            = new TreeMap<>(Classes::compareByExtends);
    private PrintStream warning = System.err;
    private final Features features = new Features();
    private Consumer<Data> returnHook = x -> {
    };

    public RuntimeContextBuilder registerMetaProperty(Object property, Function<MetaData, Object> function) {
        metaProperties.put(property, function);
        return this;
    }

    public RuntimeContextBuilder registerTextFormatter(String name, TextFormatter<?, ?> formatter) {
        textFormatterMap.put(name, formatter);
        return this;
    }

    public DALRuntimeContext build(Object inputValue) {
        return build(() -> inputValue, null);
    }

    public DALRuntimeContext build(InputCode<?> inputSupplier) {
        return build(inputSupplier, null);
    }

    public DALRuntimeContext build(InputCode<?> inputSupplier, Class<?> rootSchema) {
        if (inputSupplier == null)
            return new DALRuntimeContext(() -> null, rootSchema);
        return new DALRuntimeContext(inputSupplier, rootSchema);
    }

    public RuntimeContextBuilder registerValueFormat(Formatter<?, ?> formatter) {
        return registerValueFormat(formatter.getFormatterName(), formatter);
    }

    @SuppressWarnings("unchecked")
    public RuntimeContextBuilder registerValueFormat(String name, Formatter<?, ?> formatter) {
        valueConstructors.put(name, (o, c) -> ((Formatter<Object, ?>) formatter).transform(o.instance()));
        return this;
    }

    public RuntimeContextBuilder registerSchema(Class<? extends Schema> schema) {
        return registerSchema(NameStrategy.SIMPLE_NAME, schema);
    }

    @SuppressWarnings("unchecked")
    public RuntimeContextBuilder registerSchema(String name, Class<? extends Schema> schema) {
        schemas.put(name, BeanClass.create(schema));
        return registerSchema(name, (data, context) ->
                expect(new Expect(BeanClass.create((Class) schema), null)).verify(context, actual(data)));
    }

    public RuntimeContextBuilder registerSchema(String name, BiFunction<Data, DALRuntimeContext, Boolean> predicate) {
        valueConstructors.put(name, (o, context) -> {
            if (predicate.apply(o, context))
                return o.instance();
            throw new IllegalTypeException();
        });
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> RuntimeContextBuilder registerPropertyAccessor(Class<T> type, PropertyAccessor<? extends T> propertyAccessor) {
        propertyAccessors.put(type, (PropertyAccessor<Object>) propertyAccessor);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T, E> RuntimeContextBuilder registerDALCollectionFactory(Class<T> type, DALCollectionFactory<T, E> DALCollectionFactory) {
        dALCollectionFactories.put(type, (DALCollectionFactory<Object, Object>) DALCollectionFactory);
        return this;
    }

    public RuntimeContextBuilder registerSchema(NameStrategy nameStrategy, Class<? extends Schema> schema) {
        return registerSchema(nameStrategy.toName(schema), schema);
    }

    public RuntimeContextBuilder registerStaticMethodExtension(Class<?> staticMethodExtensionClass) {
        Stream.of(staticMethodExtensionClass.getMethods()).filter(method -> method.getParameterCount() >= 1
                && (STATIC & method.getModifiers()) != 0).forEach(extensionMethods::add);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> RuntimeContextBuilder registerImplicitData(Class<T> type, Function<T, Object> mapper) {
        objectImplicitMapper.put(type, (Function) mapper);
        return this;
    }

    public Converter getConverter() {
        return converter;
    }

    public RuntimeContextBuilder setConverter(Converter converter) {
        this.converter = converter;
        return this;
    }

    public RuntimeContextBuilder registerUserDefinedLiterals(UserLiteralRule rule) {
        userDefinedLiterals.add(rule);
        return this;
    }

    public RuntimeContextBuilder registerCurryingMethodAvailableParameters(Method method, BiFunction<Object,
            List<Object>, List<Object>> range) {
        curryingMethodArgRanges.put(method, range);
        return this;
    }

    private Set<Method> methodToCurrying(Class<?> type, Object methodName) {
        return Stream.of(stream(type.getMethods()).filter(method -> !Modifier.isStatic(method.getModifiers()))
                                .filter(method -> method.getName().equals(methodName)),
                        staticMethodsToCurrying(type, methodName, Object::equals),
                        staticMethodsToCurrying(type, methodName, Class::isAssignableFrom))
                .flatMap(Function.identity()).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Stream<Method> staticMethodsToCurrying(Class<?> type, Object property,
                                                   BiPredicate<Class<?>, Class<?>> condition) {
        return extensionMethods.stream()
                .filter(method -> staticExtensionMethodName(method).equals(property))
                .filter(method -> condition.test(method.getParameters()[0].getType(), type));
    }

    private static String staticExtensionMethodName(Method method) {
        ExtensionName extensionName = method.getAnnotation(ExtensionName.class);
        return extensionName != null ? extensionName.value() : method.getName();
    }

    BiFunction<Object, List<Object>, List<Object>> fetchCurryingMethodArgRange(Method method) {
        return curryingMethodArgRanges.get(method);
    }

    public CheckerSet checkerSetForMatching() {
        return checkerSetForMatching;
    }

    public CheckerSet checkerSetForEqualing() {
        return checkerSetForEqualing;
    }

    public RuntimeContextBuilder registerDumper(Class<?> type, DumperFactory factory) {
        dumperFactories.put(type, factory);
        return this;
    }

    public void setMaxDumpingLineSize(int size) {
        maxDumpingLineSize = size;
    }

    public <T> RuntimeContextBuilder registerErrorHook(ErrorHook hook) {
        errorHook = Objects.requireNonNull(hook);
        return this;
    }

    public void mergeTextFormatter(String name, String other, String... others) {
        TextFormatter formatter = textFormatterMap.get(other);
        for (String o : others)
            formatter = formatter.merge(textFormatterMap.get(o));
        registerTextFormatter(name, delegateFormatter(formatter, "Merged from " + other + " " + String.join(" ", others)));
    }

    private TextFormatter delegateFormatter(TextFormatter formatter, final String description) {
        return new TextFormatter() {
            @Override
            protected Object format(Object content, TextAttribute attribute, DALRuntimeContext context) {
                return formatter.format(content, attribute, context);
            }

            @Override
            protected TextAttribute attribute(TextAttribute attribute) {
                return formatter.attribute(attribute);
            }

            @Override
            public Class<?> returnType() {
                return formatter.returnType();
            }

            @Override
            public Class<?> acceptType() {
                return formatter.acceptType();
            }

            @Override
            public String description() {
                return description;
            }
        };
    }

    public RuntimeContextBuilder registerMetaProperty(Class<?> type, Object name, Function<MetaData, Object> function) {
        localMetaProperties.computeIfAbsent(type, k -> new HashMap<>()).put(name, function);
        return this;
    }

    public RuntimeContextBuilder registerDataRemark(Class<?> type, Function<RemarkData, Object> action) {
        remarks.put(type, action);
        return this;
    }

    public RuntimeContextBuilder registerExclamation(Class<?> type, Function<RuntimeData, Object> action) {
        exclamations.put(type, action);
        return this;
    }

    public RuntimeContextBuilder registerOperator(Operators operator, Operation operation) {
        operations.computeIfAbsent(operator, o -> new LinkedList<>()).addFirst(operation);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> RuntimeContextBuilder registerCustomSorter(Class<T> type, Function<T, Comparable<?>> sorter) {
        customSorters.put(type, (Function<Object, Comparable<?>>) sorter);
        return this;
    }

    public BeanClass<?> schemaType(String schema) {
        BeanClass<?> type = schemas.get(schema);
        if (type != null)
            return type;
        throw new IllegalStateException(format("Unknown schema '%s'", schema));
    }

    public void setMaxDumpingObjectSize(int maxDumpingObjectSize) {
        this.maxDumpingObjectSize = maxDumpingObjectSize;
    }

    public RuntimeContextBuilder setWarningOutput(PrintStream printStream) {
        warning = printStream;
        return this;
    }

    public RuntimeContextBuilder registerReturnHook(Consumer<Data> hook) {
        returnHook = returnHook.andThen(hook);
        return this;
    }

    public Features features() {
        return features;
    }

    public class DALRuntimeContext implements RuntimeContext {
        private final LinkedList<Data> stack = new LinkedList<>();
        private final Map<Data, PartialPropertyStack> partialPropertyStacks;

        public Features features() {
            return features;
        }

        public DALRuntimeContext(InputCode<?> supplier, Class<?> schema) {
            BeanClass<?> rootSchema = null;
            if (schema != null)
                rootSchema = BeanClass.create(schema);
            stack.push(data(() -> {
                try {
                    return supplier.get();
                } catch (Exception e) {
                    throw new UserRuntimeException(e);
                }
            }, rootSchema));
            partialPropertyStacks = new HashMap<>();
        }

        public Data getThis() {
            return stack.getFirst();
        }

        public <T> T pushAndExecute(Data data, Supplier<T> supplier) {
            try {
                stack.push(data);
                return supplier.get();
            } finally {
                returnHook.accept(stack.pop());
            }
        }

        public Optional<ConstructorViaSchema> searchValueConstructor(String type) {
            return Optional.ofNullable(valueConstructors.get(type));
        }

        public Set<?> findPropertyReaderNames(Object instance) {
            return getObjectPropertyAccessor(instance).getPropertyNames(instance);
        }

        public PropertyAccessor<Object> getObjectPropertyAccessor(Object instance) {
            return propertyAccessors.tryGetData(instance)
                    .orElseGet(() -> new JavaClassPropertyAccessor<>(BeanClass.createFrom(instance)));
        }

        public Boolean isNull(Object instance) {
            return propertyAccessors.tryGetData(instance).map(f -> f.isNull(instance))
                    .orElseGet(() -> Objects.equals(instance, null));
        }

        public DALCollection<Object> createCollection(Object instance) {
            return dALCollectionFactories.tryGetData(instance).map(factory -> factory.create(instance))
                    .orElseGet(() -> new CollectionDALCollection<>(toStream(instance).collect(toList())));
        }

        public boolean isRegisteredList(Object instance) {
            return dALCollectionFactories.tryGetData(instance).map(f -> f.isList(instance)).orElse(false);
        }

        public Converter getConverter() {
            return converter;
        }

        public Data data(ThrowingSupplier<?> instance, String schema, boolean isList) {
            BeanClass<?> schemaType = schemas.get(schema);
            if (isList && schemaType != null)
                schemaType = BeanClass.create(Array.newInstance(schemaType.getType(), 0).getClass());
            return data(instance, schemaType);
        }

        public Data data(ThrowingSupplier<?> supplier, BeanClass<?> schemaType) {
            return new Data(supplier == null ? () -> null : supplier, this, SchemaType.create(schemaType));
        }

        public Data data(ThrowingSupplier<?> instance) {
            return data(instance, null);
        }

        public Data data(Object instance) {
            return data(() -> instance, null);
        }

        public Optional<Result> takeUserDefinedLiteral(String token) {
            return userDefinedLiterals.stream().map(userLiteralRule -> userLiteralRule.compile(token))
                    .filter(Result::hasResult)
                    .findFirst();
        }

        public void appendPartialPropertyReference(Data data, Object symbol) {
            fetchPartialProperties(data).map(partialProperties -> partialProperties.appendPartialProperties(symbol));
        }

        private Optional<PartialProperties> fetchPartialProperties(Data data) {
            return partialPropertyStacks.values().stream().map(partialPropertyStack ->
                    partialPropertyStack.fetchPartialProperties(data)).filter(Objects::nonNull).findFirst();
        }

        public void initPartialPropertyStack(Data instance, Object prefix, Data partial) {
            partialPropertyStacks.computeIfAbsent(instance, _key -> fetchPartialProperties(instance)
                    .map(partialProperties -> partialProperties.partialPropertyStack)
                    .orElseGet(PartialPropertyStack::new)).setupPartialProperties(prefix, partial);
        }

        public Set<String> collectPartialProperties(Data instance) {
            PartialPropertyStack partialPropertyStack = partialPropertyStacks.get(instance);
            if (partialPropertyStack != null)
                return partialPropertyStack.collectPartialProperties(instance);
            return fetchPartialProperties(instance).map(partialProperties ->
                    partialProperties.partialPropertyStack.collectPartialProperties(instance)).orElse(emptySet());
        }

        public NumberType getNumberType() {
            return numberType;
        }

        public Optional<Object> getImplicitObject(Object obj) {
            return objectImplicitMapper.tryGetData(obj).map(mapper -> mapper.apply(obj));
        }

        public Set<Method> methodToCurrying(Class<?> type, Object methodName) {
            return RuntimeContextBuilder.this.methodToCurrying(type, methodName);
        }

        public Function<MetaData, Object> fetchGlobalMetaFunction(MetaData metaData) {
            return metaProperties.computeIfAbsent(metaData.name(), k -> {
                throw illegalOp2(format("Meta property `%s` not found", metaData.name()));
            });
        }

        private Optional<Function<MetaData, Object>> fetchLocalMetaFunction(MetaData metaData) {
            return metaFunctionsByType(metaData).map(e -> {
                metaData.addCallType(e.getKey());
                return e.getValue().get(metaData.name());
            }).filter(Objects::nonNull).findFirst();
        }

        public Optional<Function<MetaData, Object>> fetchSuperMetaFunction(MetaData metaData) {
            return metaFunctionsByType(metaData)
                    .filter(e -> !metaData.calledBy(e.getKey()))
                    .map(e -> {
                        metaData.addCallType(e.getKey());
                        return e.getValue().get(metaData.name());
                    }).filter(Objects::nonNull).findFirst();
        }

        private Stream<Map.Entry<Class<?>, Map<Object, Function<MetaData, Object>>>> metaFunctionsByType(MetaData metaData) {
            return localMetaProperties.entrySet().stream().filter(e -> metaData.isInstance(e.getKey()));
        }

        @SuppressWarnings("unchecked")
        public <T> TextFormatter<String, T> fetchFormatter(String name, int position) {
            return (TextFormatter<String, T>) textFormatterMap.computeIfAbsent(name, attribute -> {
                throw new SyntaxException(format("Invalid text formatter `%s`, all supported formatters are:\n%s",
                        attribute, textFormatterMap.entrySet().stream().map(e -> format("  %s:\n    %s",
                                e.getKey(), e.getValue().fullDescription())).collect(joining("\n"))), position);
            });
        }

        public Checker fetchEqualsChecker(Data expected, Data actual) {
            return checkerSetForEqualing.fetch(expected, actual);
        }

        public Checker fetchMatchingChecker(Data expected, Data actual) {
            return checkerSetForMatching.fetch(expected, actual);
        }

        public Dumper fetchDumper(Data.Resolved data) {
            return dumperFactories.tryGetData(data.value()).map(factory -> factory.apply(data)).orElseGet(() -> {
                if (data.isNull())
                    return (_data, dumpingContext) -> dumpingContext.append("null");
                if (data.isList())
                    return Dumper.LIST_DUMPER;
                if (data.isEnum())
                    return Dumper.VALUE_DUMPER;
                return Dumper.MAP_DUMPER;
            });
        }

        public int maxDumpingLineCount() {
            return maxDumpingLineSize;
        }

        public int maxDumpingObjectSize() {
            return maxDumpingObjectSize;
        }

        public boolean hookError(String expression, Throwable error) {
            return errorHook.handle(getThis(), expression, error);
        }

        public Data invokeMetaProperty(DALNode inputNode, Data inputData, Object symbolName) {
            return data(() -> {
                MetaData metaData = new MetaData(inputNode, inputData, symbolName, this);
                return fetchLocalMetaFunction(metaData).orElseGet(() -> fetchGlobalMetaFunction(metaData)).apply(metaData);
            }).onError(DalException::buildUserRuntimeException);
        }

        public Object invokeDataRemark(RemarkData remarkData) {
            Object instance = remarkData.data().instance();
            return remarks.tryGetData(instance)
                    .orElseThrow(() -> illegalOperation("Not implement operator () of " + getClassName(instance)))
                    .apply(remarkData);
        }

        public Object invokeExclamations(ExclamationData exclamationData) {
            Object instance = exclamationData.data().instance();
            return exclamations.tryGetData(instance)
                    .orElseThrow(() -> illegalOp2(format("Not implement operator %s of %s",
                            exclamationData.label(), Classes.getClassName(instance))))
                    .apply(exclamationData);
        }

        public Data calculate(Data v1, DALOperator opt, Data v2) {
            return data(() -> {
                for (Operation operation : operations.get(opt.overrideType()))
                    if (operation.match(v1, opt, v2, this))
                        return operation.operate(v1, opt, v2, this);
                throw illegalOperation(format("No operation `%s` between '%s' and '%s'", opt.overrideType(),
                        getClassName(v1.instance()), getClassName(v2.instance())));
            });
        }

        public PrintStream warningOutput() {
            return warning;
        }

        public BiFunction<Object, List<Object>, List<Object>> fetchCurryingMethodArgRange(Method method) {
            return curryingMethodArgRanges.get(method);
        }

        public Optional<CurryingMethod> currying(Object instance, Object property) {
            List<InstanceCurryingMethod> methods = methodToCurrying(named(instance.getClass()), property).stream()
                    .map(method -> createCurryingMethod(instance, method, getConverter(), this)).collect(toList());
            if (!methods.isEmpty())
                return of(new CurryingMethodGroup(methods, null));
            return getImplicitObject(instance).flatMap(obj -> currying(obj, property));
        }

        @SuppressWarnings("unchecked")
        public Comparable<?> transformComparable(Object object) {
            return customSorters.tryGetData(object).map(f -> f.apply(object)).orElseGet(() -> (Comparable) object);
        }
    }
}
