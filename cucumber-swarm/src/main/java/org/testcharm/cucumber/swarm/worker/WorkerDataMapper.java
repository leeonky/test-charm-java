package org.testcharm.cucumber.swarm.worker;

import io.cucumber.messages.types.Hook;
import io.cucumber.messages.types.StepDefinition;
import org.testcharm.cucumber.swarm.DataMapper;
import org.testcharm.cucumber.swarm.repo.Repository;

import java.net.URI;
import java.util.List;

public class WorkerDataMapper extends DataMapper {
    private static WorkerDataMapper instance;
    private final Repository<String, StepDefinition> stepDefinitionRepository = new Repository<>(StepDefinition::getId);
    private final Repository<String, Hook> hookRepository = new Repository<>(Hook::getId);

    public WorkerDataMapper(List<URI> featurePaths) {
        super(featurePaths);
        instance = this;
    }

    public static WorkerDataMapper instance() {
        return instance;
    }

    public void mapStepDefinition(StepDefinition stepDefinition) {
        stepDefinitionRepository.save(stepDefinition);
    }

    public StepDefinition stepDefinition(String id) {
        return stepDefinitionRepository.findByKey(id);
    }

    public void mapHook(Hook hook) {
        hookRepository.save(hook);
    }

    public Hook hook(String id) {
        return hookRepository.findByKey(id);
    }

    public String transformStepDefinitionIdToKey(String id) {
        return stepDefinitionKey(stepDefinition(id));
    }

    public String transformHookIdToKey(String id) {
        return hookKey(hook(id));
    }
}
