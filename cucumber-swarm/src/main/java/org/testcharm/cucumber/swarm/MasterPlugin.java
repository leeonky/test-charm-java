package org.testcharm.cucumber.swarm;

import io.cucumber.messages.types.Envelope;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import org.testcharm.cucumber.swarm.master.MasterDataMapper;
import org.testcharm.util.Classes;

import static java.util.stream.Stream.concat;
import static org.testcharm.util.Classes.subTypesOf;

public class MasterPlugin implements ConcurrentEventListener {

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(Envelope.class, envelop -> {
            envelop.getStepDefinition().ifPresent(stepDefinition -> MasterDataMapper.instance().mapStepDefinition(stepDefinition));
            envelop.getHook().ifPresent(hook -> MasterDataMapper.instance().mapHook(hook));
        });

        concat(subTypesOf(MasterPluginExtension.class, "org.testcharm.cucumber.extensions").stream(),
                subTypesOf(MasterPluginExtension.class, "org.testcharm.extensions.cucumber").stream())
                .map(Classes::newInstance)
                .forEach(e -> e.setEventPublisher(publisher));
    }
}
