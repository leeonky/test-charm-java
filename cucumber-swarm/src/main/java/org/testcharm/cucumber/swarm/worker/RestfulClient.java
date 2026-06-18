package org.testcharm.cucumber.swarm.worker;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import org.testcharm.cucumber.swarm.SwarmHost;
import org.testcharm.cucumber.swarm.util.IoUtil;
import org.testcharm.util.Sneaky;
import org.testcharm.util.ThrowingSupplier;

import java.net.HttpURLConnection;
import java.net.URL;

import static org.testcharm.util.function.Extension.adaptThrowing;

public class RestfulClient {
    private static final Logger log = LoggerFactory.getLogger(RestfulClient.class);
    private final SwarmHost swarmArgs;

    public RestfulClient(SwarmHost swarmArgs) {
        this.swarmArgs = swarmArgs;
    }

    public String httpGet(int workerId, String path) {
        URL url = swarmArgs.swarmUrl(path);
        return httpRequest(() -> {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("X-Worker-Id", String.valueOf(workerId));

            if (urlConnection.getResponseCode() == 200)
                return new String(IoUtil.readAll(urlConnection.getInputStream()));
            throw new HttpException(urlConnection);
        }, "GET from " + url);
    }

    public void httpPost(int workerId, String path, String body) {
        URL url = swarmArgs.swarmUrl(path);
        httpRequest(adaptThrowing(() -> {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("X-Worker-Id", String.valueOf(workerId));

            urlConnection.setDoOutput(true);
            urlConnection.getOutputStream().write(body.getBytes());

            if (urlConnection.getResponseCode() != 200)
                throw new HttpException(urlConnection);
        }), "POST to " + url);
    }

    private <T> T httpRequest(ThrowingSupplier<T> action, String requestInfo) {
        try {
            return action.get();
        } catch (HttpException e) {
            throw e;
        } catch (Throwable e) {
            log.error(() -> "Failed to " + requestInfo + "\n" + e);
            return Sneaky.sneakyThrow(e);
        }
    }
}
