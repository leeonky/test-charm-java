package org.testcharm.cucumber.swarm.master;

import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.runner.TestCaseFactory;
import io.cucumber.messages.types.Hook;
import io.cucumber.messages.types.StepDefinition;
import io.cucumber.plugin.event.TestCase;
import org.testcharm.cucumber.swarm.DataMapper;
import org.testcharm.cucumber.swarm.repo.Repository;

import java.net.URI;
import java.util.Collection;
import java.util.List;

public class MasterDataMapper extends DataMapper {
    private final Repository<String, TestCase> testCaseRepository = new Repository<>(this::testCaseKey);
    private final TestCaseFactory testCaseFactory;
    private final Repository<String, StepDefinition> stepDefinitionRepository = new Repository<>(this::stepDefinitionKey);
    private final Repository<String, Hook> hookRepository = new Repository<>(this::hookKey);

    private static MasterDataMapper instance;

    public static MasterDataMapper instance() {
        return instance;
    }

    public MasterDataMapper(List<URI> featurePaths, TestCaseFactory testCaseFactory) {
        super(featurePaths);
        this.testCaseFactory = testCaseFactory;
        instance = this;
    }

    public void mapTestCase(TestCase testCase) {
        testCaseRepository.save(testCase);
    }

    public void mapHook(Hook hook) {
        hookRepository.save(hook);
    }

    public TestCase testCase(String key) {
        return testCaseRepository.findByKey(key);
    }

    public Collection<TestCase> testCases() {
        return testCaseRepository.findAll();
    }

    public Collection<StepDefinition> stepDefinitions() {
        return stepDefinitionRepository.findAll();
    }

    public Collection<Hook> hooks() {
        return hookRepository.findAll();
    }

    @Override
    public void mapGherkinPickle(Pickle pickle) {
        super.mapGherkinPickle(pickle);
        mapTestCase(testCaseFactory.createTestCaseForPickle(pickle));
    }

    public void mapStepDefinition(StepDefinition stepDefinition) {
        stepDefinitionRepository.save(stepDefinition);
    }

    public StepDefinition stepDefinition(String key) {
        return stepDefinitionRepository.findByKey(key);
    }

    public Hook hook(String key) {
        return hookRepository.findByKey(key);
    }
}
