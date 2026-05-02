package org.testcharm.cucumber.swarm;

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
}
