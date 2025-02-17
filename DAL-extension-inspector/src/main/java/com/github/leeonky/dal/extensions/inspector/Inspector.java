package com.github.leeonky.dal.extensions.inspector;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.util.Suppressor;
import de.neuland.pug4j.PugConfiguration;
import de.neuland.pug4j.template.TemplateLoader;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.rendering.JavalinRenderer;
import io.javalin.websocket.WsContext;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;

public class Inspector {
    public static InspectorCore inspectorBk;
    private static Inspector inspector = null;

    private final Javalin javalin;
    private CountDownLatch serverReadyLatch;
    private final Set<DAL> instances = new LinkedHashSet<>();
    private final Map<String, WsContext> clientConnections = new ConcurrentHashMap<>();

    public Inspector() {
        PugConfiguration pugConfiguration = new PugConfiguration();
        pugConfiguration.setTemplateLoader(new TemplateLoader() {
            @Override
            public long getLastModified(String name) throws IOException {
                return -1;
            }

            @Override
            public Reader getReader(String name) throws IOException {
                return new InputStreamReader(requireNonNull(currentThread().getContextClassLoader().getResourceAsStream(name)),
                        StandardCharsets.UTF_8);
            }

            @Override
            public String getExtension() {
                return "pug";
            }

            @Override
            public String getBase() {
                return "";
            }
        });

        JavalinRenderer.register((filePath, model, context) ->
                pugConfiguration.renderTemplate(pugConfiguration.getTemplate("public/" + filePath), model), ".pug", ".PNG", ".Png");

        serverReadyLatch = new CountDownLatch(1);
        javalin = Javalin.create(config -> config.addStaticFiles("/public", Location.CLASSPATH))
                .events(event -> event.serverStarted(serverReadyLatch::countDown));
        requireNonNull(javalin.jettyServer()).setServerPort(10081);
        javalin.get("/", ctx -> ctx.render("/index.pug", Collections.emptyMap()));
        javalin.ws("/ws/exchange", ws -> {
            ws.onConnect(ctx -> {
                clientConnections.put(ctx.getSessionId(), ctx);
                sendInstances(ctx);
            });
            ws.onClose(ctx -> clientConnections.remove(ctx.getSessionId()));
        });
        javalin.start();
        Suppressor.run(serverReadyLatch::await);
    }

    public static void register(DAL dal) {
        inspector.addInstance(dal);
    }

    private void addInstance(DAL dal) {
        instances.add(dal);
        for (WsContext ctx : clientConnections.values()) {
            sendInstances(ctx);
        }
    }

    private void sendInstances(WsContext ctx) {
        ctx.send(ObjectWriter.serialize(new HashMap<String, Iterable<String>>() {{
            put("instances", instances.stream().map(DAL::getName).collect(Collectors.toSet()));
        }}));
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

    public static void main(String[] args) {
        launch();
    }
}