package org.testcharm.cucumber.swarm.worker;

import org.testcharm.cucumber.swarm.SwarmHost;
import org.testcharm.cucumber.swarm.util.IoUtil;
import org.testcharm.util.Sneaky;

import java.net.HttpURLConnection;
import java.net.URL;

public class RestfulClient {
    private final SwarmHost swarmArgs;

    public RestfulClient(SwarmHost swarmArgs) {
        this.swarmArgs = swarmArgs;
    }

    public String httpGet(int workerId, String path) {
        return Sneaky.get(() -> {
            URL url = swarmArgs.swarmUrl(path);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("X-Worker-Id", String.valueOf(workerId));

            if (urlConnection.getResponseCode() == 200)
                return new String(IoUtil.readAll(urlConnection.getInputStream()));
            throw new HttpException(urlConnection);
        });
    }

    public void httpPost(int workerId, String path, String body) {
        Sneaky.run(() -> {
            URL url = swarmArgs.swarmUrl(path);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("X-Worker-Id", String.valueOf(workerId));

            urlConnection.setDoOutput(true);
            urlConnection.getOutputStream().write(body.getBytes());

            if (urlConnection.getResponseCode() != 200)
                throw new HttpException(urlConnection);
        });
    }
}
