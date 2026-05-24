package io.cucumber.core.runtime;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.eventbus.UuidGenerator;
import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.filter.Filters;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.order.PickleOrder;
import io.cucumber.core.plugin.PluginFactory;
import io.cucumber.core.plugin.Plugins;
import io.cucumber.core.resource.ClassLoaders;
import io.cucumber.core.runner.TestCaseFactory;
import io.cucumber.plugin.Plugin;
import org.testcharm.cucumber.swarm.SwarmArgs;
import org.testcharm.cucumber.swarm.master.Master;
import org.testcharm.cucumber.swarm.master.MasterDataMapper;

import java.net.URI;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.cucumber.core.runtime.SynchronizedEventBus.synchronize;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * This is the main entry point for running Cucumber features from the CLI.
 */
public final class MasterRuntime {
    private static final Logger log = LoggerFactory.getLogger(MasterRuntime.class);

    private final ExitStatus exitStatus;

    private final Predicate<Pickle> filter;
    private final int limit;
    private final FeatureSupplier featureSupplier;
    private final PickleOrder pickleOrder;
    private final SwarmArgs swarmArgs;
    private final List<URI> featurePaths;
    private final EventBus eventBus;
    private final TestCaseFactory testCaseFactory;
    private final MasterCucumberExecutionContext context;

    private MasterRuntime(
            final ExitStatus exitStatus,
            final MasterCucumberExecutionContext context,
            final Predicate<Pickle> filter,
            final int limit,
            final FeatureSupplier featureSupplier,
            final PickleOrder pickleOrder,
            SwarmArgs swarmArgs, List<URI> featurePaths, EventBus eventBus, TestCaseFactory testCaseFactory) {
        this.filter = filter;
        this.context = context;
        this.limit = limit;
        this.featureSupplier = featureSupplier;
        this.exitStatus = exitStatus;
        this.pickleOrder = pickleOrder;
        this.swarmArgs = swarmArgs;
        this.featurePaths = featurePaths;
        this.eventBus = eventBus;
        this.testCaseFactory = testCaseFactory;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void run() {
        // Parse the features early. Don't proceed when there are lexer errors
        List<Feature> features = featureSupplier.get();
        context.runFeatures(() -> {
            features.forEach(context::beforeFeature);
            List<Pickle> pickles = features.stream()
                    .flatMap(feature -> feature.getPickles().stream())
                    .filter(filter)
                    .collect(collectingAndThen(toList(),
                            list -> pickleOrder.orderPickles(list).stream()))
                    .limit(limit > 0 ? limit : Integer.MAX_VALUE).collect(toList());
            Master master = new Master(swarmArgs, pickles, new MasterDataMapper(featurePaths, testCaseFactory), eventBus);
            context.executeAndThrow(() -> master.setupMapping(features));
            context.executeAndThrow(master::start);
            context.executeAndThrow(master::shutdown);
        });
    }

    public byte exitStatus() {
        return exitStatus.exitStatus();
    }

    public static class Builder {

        private EventBus eventBus;
        private Supplier<ClassLoader> classLoader = ClassLoaders::getDefaultClassLoader;
        private RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
        private BackendSupplier backendSupplier;
        private ObjectFactorySupplier objectFactorySupplier;
        private FeatureSupplier featureSupplier;
        private List<Plugin> additionalPlugins = emptyList();
        private Supplier<UuidGenerator> uuidGeneratorSupplier;

        private Builder() {
        }

        public Builder withRuntimeOptions(RuntimeOptions runtimeOptions) {
            this.runtimeOptions = runtimeOptions;
            return this;
        }

        public Builder withClassLoader(Supplier<ClassLoader> classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        public Builder withBackendSupplier(BackendSupplier backendSupplier) {
            this.backendSupplier = backendSupplier;
            return this;
        }

        public Builder withObjectFactorySupplier(ObjectFactorySupplier objectFactorySupplier) {
            this.objectFactorySupplier = objectFactorySupplier;
            return this;
        }

        public Builder withFeatureSupplier(FeatureSupplier featureSupplier) {
            this.featureSupplier = featureSupplier;
            return this;
        }

        public Builder withUuidGeneratorSupplier(Supplier<UuidGenerator> uuidGenerator) {
            uuidGeneratorSupplier = uuidGenerator;
            return this;
        }

        public Builder withAdditionalPlugins(Plugin... plugins) {
            additionalPlugins = Arrays.asList(plugins);
            return this;
        }

        public Builder withEventBus(EventBus eventBus) {
            this.eventBus = eventBus;
            return this;
        }

        public MasterRuntime build(SwarmArgs swarmArgs) {
            EventBus eventBus = synchronize(createEventBus());
            ExitStatus exitStatus = createPluginsAndExitStatus(eventBus);
            MasterCucumberExecutionContext context = new MasterCucumberExecutionContext(eventBus, exitStatus);
            Predicate<Pickle> filter = new Filters(runtimeOptions);
            int limit = runtimeOptions.getLimitCount();
            FeatureSupplier featureSupplier = createFeatureSupplier(eventBus);
            PickleOrder pickleOrder = runtimeOptions.getPickleOrder();

            ObjectFactorySupplier objectFactorySupplier = createObjectFactorySupplier();
            BackendSupplier backendSupplier = createBackendSupplier(objectFactorySupplier);
            TestCaseFactory testCaseFactory = new TestCaseFactory(eventBus, runtimeOptions, backendSupplier.get());
            return new MasterRuntime(exitStatus, context, filter, limit, featureSupplier, pickleOrder, swarmArgs, runtimeOptions.getFeaturePaths(),
                    eventBus, testCaseFactory);
        }

        private ExitStatus createPluginsAndExitStatus(EventBus eventBus) {
            Plugins plugins = createPlugins();
            ExitStatus exitStatus = new ExitStatus(runtimeOptions);
            plugins.addPlugin(exitStatus);

            if (runtimeOptions.isMultiThreaded()) {
                plugins.setSerialEventBusOnEventListenerPlugins(eventBus);
            } else {
                plugins.setEventBusOnEventListenerPlugins(eventBus);
            }
            return exitStatus;
        }

        private RunnerSupplier createRunnerSupplier(EventBus eventBus) {
            ObjectFactorySupplier objectFactorySupplier = createObjectFactorySupplier();
            BackendSupplier backendSupplier = createBackendSupplier(objectFactorySupplier);
            return runtimeOptions.isMultiThreaded()
                    ? new ThreadLocalRunnerSupplier(runtimeOptions, eventBus, backendSupplier, objectFactorySupplier)
                    : new SingletonRunnerSupplier(runtimeOptions, eventBus, backendSupplier, objectFactorySupplier);
        }

        private ObjectFactorySupplier createObjectFactorySupplier() {
            if (objectFactorySupplier != null) {
                return objectFactorySupplier;
            }
            ObjectFactoryServiceLoader objectFactoryServiceLoader = new ObjectFactoryServiceLoader(classLoader,
                    runtimeOptions);
            return runtimeOptions.isMultiThreaded()
                    ? new ThreadLocalObjectFactorySupplier(objectFactoryServiceLoader)
                    : new SingletonObjectFactorySupplier(objectFactoryServiceLoader);
        }

        private BackendSupplier createBackendSupplier(ObjectFactorySupplier objectFactorySupplier) {
            return backendSupplier != null
                    ? backendSupplier
                    : new BackendServiceLoader(classLoader, objectFactorySupplier);
        }

        private EventBus createEventBus() {
            if (eventBus != null) {
                return eventBus;
            }
            UuidGenerator uuidGenerator = createUuidGenerator();
            return new TimeServiceEventBus(Clock.systemUTC(), uuidGenerator);
        }

        private UuidGenerator createUuidGenerator() {
            if (uuidGeneratorSupplier != null) {
                return uuidGeneratorSupplier.get();
            } else {
                return new UuidGeneratorServiceLoader(classLoader, runtimeOptions).loadUuidGenerator();
            }
        }

        private FeatureSupplier createFeatureSupplier(EventBus eventBus) {
            if (featureSupplier != null) {
                return featureSupplier;
            }
            FeatureParser parser = new FeatureParser(eventBus::generateId);
            return new FeaturePathFeatureSupplier(classLoader, runtimeOptions, parser);
        }

        private Plugins createPlugins() {
            Plugins plugins = new Plugins(new PluginFactory(), runtimeOptions);
            for (Plugin plugin : additionalPlugins) {
                plugins.addPlugin(plugin);
            }
            return plugins;
        }
    }

}
