package org.testcharm.cucumber.swarm.repo;

import org.testcharm.cucumber.swarm.master.WorkerDeprecated;

@Deprecated
public class WorkerDeprecatedRepository extends Repository<Integer, WorkerDeprecated> {
    public WorkerDeprecatedRepository() {
        super(WorkerDeprecated::id);
    }
}
