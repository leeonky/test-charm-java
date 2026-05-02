package org.testcharm.cucumber.swarm;

import org.testcharm.util.Sneaky;

import java.net.URL;

public class SwarmArg {
    private int port = 10083;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private String host = "localhost";

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public URL swarmUrl(String path) {
        return Sneaky.get(() -> new URL("http://" + getHost() + ":" + getPort() + path));
    }
}
