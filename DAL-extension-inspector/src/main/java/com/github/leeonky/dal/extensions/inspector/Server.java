package com.github.leeonky.dal.extensions.inspector;

import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.ClasspathTemplateLoader;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.rendering.JavalinRenderer;

import java.util.Collections;

public class Server {
    public static final Server INSTANCE = new Server();
    private final Javalin javalin;
    private Javalin app;

    public Server() {
        JadeConfiguration jadeConfiguration = new JadeConfiguration();
        jadeConfiguration.setCaching(false);
        jadeConfiguration.setTemplateLoader(new ClasspathTemplateLoader());
        JavalinRenderer.register((filePath, model, context) ->
                jadeConfiguration.renderTemplate(jadeConfiguration.getTemplate("public" + filePath), model), ".pug", ".PNG", ".Png");
        javalin = Javalin.create(config -> config.addStaticFiles("/public", Location.CLASSPATH));
    }

    public void start() {
        synchronized (Server.class) {
            if (app == null) {
                app = javalin.start(7000);
                app.get("/", ctx -> ctx.render("/index.pug", Collections.emptyMap()));
            }
        }
    }

    public void stop() {
        synchronized (Server.class) {
            if (app != null) {
                app.close();
                app = null;
            }
        }
    }
}
