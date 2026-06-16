package org.testcharm.cucumber.swarm.master;

import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.testcharm.dal.Assertions.expect;

class SocketProbeTest {

    @Test
    void test_connection() {
        SocketProbe probe = new SocketProbe("Test Server", new InetSocketAddress("localhost", 60000), Duration.ofSeconds(1));
        expect(assertThrows(IllegalStateException.class, probe::testConnection).getMessage()).isEqualTo("Test Server did not become ready within PT1S");
    }
}