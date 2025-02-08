package com.github.leeonky.dal.extensions.inspector;

import com.github.leeonky.util.Suppressor;
import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.ClasspathTemplateLoader;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.rendering.JavalinRenderer;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class HttpServer {
    private final InspectorCore.ApiProvider apiProvider;
    private Javalin javalin;
    private CountDownLatch serverReadyLatch;

    public HttpServer(InspectorCore.ApiProvider apiProvider) {
        this.apiProvider = apiProvider;
        JadeConfiguration jadeConfiguration = new JadeConfiguration();
        jadeConfiguration.setCaching(false);
        jadeConfiguration.setTemplateLoader(new ClasspathTemplateLoader());
        JavalinRenderer.register((filePath, model, context) ->
                jadeConfiguration.renderTemplate(jadeConfiguration.getTemplate("public" + filePath), model), ".pug", ".PNG", ".Png");
    }

    public HttpServer start() {
        serverReadyLatch = new CountDownLatch(1);
        javalin = Javalin.create(config -> config.addStaticFiles("/public", Location.CLASSPATH))
                .events(event -> event.serverStarted(serverReadyLatch::countDown));
        Objects.requireNonNull(javalin.jettyServer()).setServerPort(10081);
        javalin.get("/", ctx -> ctx.render("/index.pug", Collections.emptyMap()));
        javalin.get("/api/fetch-code", ctx -> ctx.status(200).html(apiProvider.fetchCode()));
        javalin.post("/api/execute", ctx -> ctx.status(200).html(apiProvider.execute(ctx.body())));
        javalin.start();
        Suppressor.run(serverReadyLatch::await);
        return this;
    }

    public void stop() {
        javalin.close();
    }
}