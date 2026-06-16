package org.testcharm.cucumber.swarm.master;

import org.testcharm.util.Sneaky;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;

public class SocketProbe {
    private final Duration timeout;
    private final InetSocketAddress target;
    private final String name;

    public SocketProbe(String serverName, InetSocketAddress serverAddress, Duration timeout) {
        this.timeout = timeout;
        target = serverAddress;
        name = serverName;
    }

    void testConnection() {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            try (Socket socket = new Socket()) {
                socket.connect(target, 200);
                return;
            } catch (IOException e) {
                Sneaky.run(() -> Thread.sleep(50));
            }
        }
        throw new IllegalStateException(name + " did not become ready within " + timeout);
    }
}
