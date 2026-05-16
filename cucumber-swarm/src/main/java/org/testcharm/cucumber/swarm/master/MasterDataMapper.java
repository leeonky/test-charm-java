package org.testcharm.cucumber.swarm.master;

import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.runner.TestCaseFactory;
import io.cucumber.plugin.event.TestCase;
import org.testcharm.cucumber.swarm.DataMapper;
import org.testcharm.cucumber.swarm.repo.Repository;

import java.net.URI;
import java.util.Collection;
import java.util.List;

public class MasterDataMapper extends DataMapper {
    private final Repository<String, TestCase> testCaseRepository = new Repository<>(this::testCaseKey);
    private final TestCaseFactory testCaseFactory;

    private static MasterDataMapper instanceForTest;

    public static MasterDataMapper getInstanceForTest() {
        return instanceForTest;
    }

    public MasterDataMapper(List<URI> featurePaths, TestCaseFactory testCaseFactory) {
        super(featurePaths);
        this.testCaseFactory = testCaseFactory;
        instanceForTest = this;
    }

    public void mapTestCase(TestCase testCase) {
        testCaseRepository.save(testCase);
    }

    public TestCase testCase(String key) {
        return testCaseRepository.findByKey(key);
    }

    public Collection<TestCase> testCases() {
        return testCaseRepository.findAll();
    }

    @Override
    public void mapGherkinPickle(Pickle pickle) {
        super.mapGherkinPickle(pickle);
        mapTestCase(testCaseFactory.createTestCaseForPickle(pickle));
    }
}
