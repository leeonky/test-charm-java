package io.cucumber.core.runtime;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.eventbus.UuidGenerator;
import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.plugin.PluginFactory;
import io.cucumber.core.plugin.Plugins;
import io.cucumber.core.resource.ClassLoaders;
import io.cucumber.plugin.Plugin;
import org.testcharm.cucumber.swarm.master.Server;
import org.testcharm.cucumber.swarm.worker.Remote;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static io.cucumber.core.runtime.SynchronizedEventBus.synchronize;
import static java.util.Collections.emptyList;
import static org.testcharm.cucumber.swarm.master.Worker.NO_PICKLE;
import static org.testcharm.cucumber.swarm.worker.Remote.REMOTE;

/**
 * This is the main entry point for running Cucumber features from the CLI.
 */
public final class WorkerRuntime {

    private static final Logger log = LoggerFactory.getLogger(WorkerRuntime.class);

    private final ExitStatus exitStatus;

    private final FeatureSupplier featureSupplier;
    private final CucumberExecutionContext context;

    private WorkerRuntime(
            final ExitStatus exitStatus,
            final CucumberExecutionContext context,
            final FeatureSupplier featureSupplier,
            Server server
    ) {
        this.context = context;
        this.featureSupplier = featureSupplier;
        this.exitStatus = exitStatus;
        Remote.setupRemote(server);
    }

    public static Builder builder() {
        return new Builder();
    }

    public void run() {
        // Parse the features early. Don't proceed when there are lexer errors
        List<Feature> features = featureSupplier.get();
        context.runFeatures(() -> runFeatures(features));
    }

    private void runFeatures(List<Feature> features) {
        features.forEach(context::beforeFeature);
        if (REMOTE.register()) {
            for (; ; ) {
                Pickle pickle = REMOTE.requestPickle();
                if (pickle == NO_PICKLE)
                    break;
                context.runTestCase(runner -> runner.runPickle(pickle));
            }
        }
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

        public WorkerRuntime build(Server server) {
            EventBus eventBus = synchronize(createEventBus());
            ExitStatus exitStatus = createPluginsAndExitStatus(eventBus);
            RunnerSupplier runnerSupplier = createRunnerSupplier(eventBus);
            CucumberExecutionContext context = new CucumberExecutionContext(eventBus, exitStatus, runnerSupplier);
            FeatureSupplier featureSupplier = createFeatureSupplier(eventBus);
            return new WorkerRuntime(exitStatus, context, featureSupplier, server);
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
