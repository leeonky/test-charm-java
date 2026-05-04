package org.testcharm.cucumber.swarm.worker;

import org.testcharm.cucumber.swarm.SwarmHost;
import org.testcharm.util.Sneaky;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class Client {
    private final SwarmHost swarmArgs;

    public Client(SwarmHost swarmArgs) {
        this.swarmArgs = swarmArgs;
    }

//    public String requestPickle(int workerId) {
//        return Sneaky.get(() -> {
//            URL url = swarmArgs.swarmUrl("/pickle");
//
//            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//            urlConnection.setRequestMethod("GET");
//            urlConnection.setRequestProperty("Content-Type", "application/json");
//            urlConnection.setRequestProperty("X-Worker-Id", String.valueOf(workerId));
//
//            return new String(readAll(urlConnection.getInputStream()));
//        });
//

    /// /        return server.requestPickle(workerId);
//    }
    private static byte[] readAll(InputStream stream) {
        return Sneaky.get(() -> {
            try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                int size;
                byte[] data = new byte[1024];
                while ((size = stream.read(data, 0, data.length)) != -1)
                    buffer.write(data, 0, size);
                return buffer.toByteArray();
            }
        });
    }

//    public Optional<Integer> register() {
//        return Sneaky.get(() -> {
//            URL url = swarmArgs.swarmUrl("/register");
//
//            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//            urlConnection.setRequestMethod("POST");
//            urlConnection.setRequestProperty("Content-Type", "application/json");
//
//            if (urlConnection.getResponseCode() == 200)
//                return Optional.of(Integer.valueOf(new String(readAll(urlConnection.getInputStream()))));
//            else
//                return Optional.empty();
//
//        });
//    }

//    public void sendEvent(int workerId, Object event) {
//        server.receiveEvent(workerId, event);
//    }
}
