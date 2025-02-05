package com.github.leeonky.dal.extensions.debug;

import io.javalin.Javalin;

public class Debugger {
    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7000);
        app.get("/", ctx -> ctx.result("Hello, Javalin!"));

        app.get("/hello/{name}", ctx -> {
            String name = ctx.pathParam("name");
            ctx.result("Hello, " + name + "!");
        });

        return;
    }
}
