package org.testcharm.cucumber.restful;

import org.testcharm.jfactory.JFactory;
import org.testcharm.jfactory.JFactoryCollector;
import org.testcharm.util.Collector;

import java.util.Map;

public class RequestCollector extends JFactoryCollector {
    private final Collector headerCollector;
    private final RestfulStep.Request.RequestContext requestContext;

    protected RequestCollector(JFactory jFactory, RestfulStep.Request.RequestContext requestContext) {
        super(jFactory, Object.class);
        headerCollector = jFactory.collector();
        this.requestContext = requestContext;
    }

    public Collector headerCollector() {
        return headerCollector;
    }

    public Map<String, RestfulStep.UploadFile> files() {
        return requestContext.getFiles();
    }
}
