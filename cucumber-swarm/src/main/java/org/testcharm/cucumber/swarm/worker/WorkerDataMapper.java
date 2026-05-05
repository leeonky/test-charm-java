package org.testcharm.cucumber.swarm.worker;

import org.testcharm.cucumber.swarm.DataMapper;

import java.net.URI;
import java.util.List;

public class WorkerDataMapper extends DataMapper {

    public WorkerDataMapper(List<URI> featurePaths) {
        super(featurePaths);
    }

}
