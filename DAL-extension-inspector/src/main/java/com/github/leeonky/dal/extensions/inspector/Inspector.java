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

public class Inspector {
    public static InspectorCore inspectorBk;

    private static Inspector inspector = null;

    private Javalin javalin;
    private CountDownLatch serverReadyLatch;

    public Inspector() {
        JadeConfiguration jadeConfiguration = new JadeConfiguration();
        jadeConfiguration.setCaching(false);
        jadeConfiguration.setTemplateLoader(new ClasspathTemplateLoader());
        JavalinRenderer.register((filePath, model, context) ->
                jadeConfiguration.renderTemplate(jadeConfiguration.getTemplate("public" + filePath), model), ".pug", ".PNG", ".Png");

        serverReadyLatch = new CountDownLatch(1);
        javalin = Javalin.create(config -> config.addStaticFiles("/public", Location.CLASSPATH))
                .events(event -> event.serverStarted(serverReadyLatch::countDown));
        Objects.requireNonNull(javalin.jettyServer()).setServerPort(10081);
        javalin.get("/", ctx -> ctx.render("/index.pug", Collections.emptyMap()));
//        javalin.get("/api/sync", ctx -> ctx.status(200).html(apiProvider.sync()));
//        javalin.post("/api/resume", ctx -> apiProvider.resume());
//        javalin.post("/api/execute", ctx -> ctx.status(200).html(apiProvider.execute(ctx.body())));
//        javalin.ws("/ws/ping", ws -> {
//            ws.onConnect(ctx -> {
//                clientConnections.put(ctx.getSessionId(), ctx);
//                if (running)
//                    for (WsContext wsContext : clientConnections.values()) {
//                        wsContext.send("start");
//                    }
//            });
//            ws.onClose(ctx -> clientConnections.remove(ctx.getSessionId()));
//        });
        javalin.start();
        Suppressor.run(serverReadyLatch::await);
    }

    private void stop() {
        javalin.close();
    }

    public static void launch() {
        if (inspector == null) {
            inspector = new Inspector();
        }
    }

    public static void shutdown() {
        if (inspector != null) {
            inspector.stop();
            inspector = null;
        }
    }

//    public static InspectorCore inspector() {
//        return Objects.requireNonNull(inspector);
//    }

//    public static final Set<DAL> instances = new LinkedHashSet<>();
//
//    public static Set<DAL> getInstances() {
//        return instances;
//    }

    public static void main(String[] args) {
        launch();
    }
}