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

public class Server {
    public static final Server INSTANCE = new Server();
    private Javalin javalin;
    private CountDownLatch serverReadyLatch;

    public Server() {
        JadeConfiguration jadeConfiguration = new JadeConfiguration();
        jadeConfiguration.setCaching(false);
        jadeConfiguration.setTemplateLoader(new ClasspathTemplateLoader());
        JavalinRenderer.register((filePath, model, context) ->
                jadeConfiguration.renderTemplate(jadeConfiguration.getTemplate("public" + filePath), model), ".pug", ".PNG", ".Png");

    }

    public void start() {
        synchronized (Server.class) {
            if (javalin == null) {
                serverReadyLatch = new CountDownLatch(1);
                javalin = Javalin.create(config -> config.addStaticFiles("/public", Location.CLASSPATH))
                        .events(event -> event.serverStarted(serverReadyLatch::countDown));
                Objects.requireNonNull(javalin.jettyServer()).setServerPort(10081);
                javalin.get("/", ctx -> ctx.render("/index.pug", Collections.emptyMap()));
                javalin.start();
                Suppressor.run(serverReadyLatch::await);
            }
        }
    }

    public void stop() {
        synchronized (Server.class) {
            if (javalin != null) {
                javalin.close();
                javalin = null;
            }
        }
    }
}
