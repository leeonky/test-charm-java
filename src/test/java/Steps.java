import com.github.dreamhead.moco.RequestHit;
import com.github.dreamhead.moco.Runner;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.URL;

import static com.github.dreamhead.moco.Moco.*;
import static com.github.dreamhead.moco.MocoRequestHit.once;
import static com.github.dreamhead.moco.MocoRequestHit.requestHit;
import static com.github.dreamhead.moco.Runner.runner;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class Steps {
    private String baseUrl = null;
    private OkHttpClient httpClient;
    private Runner runner = null;
    private RequestHit requestHit;
    private String requestedBaseUrl;

    @Given("base url {string}")
    public void base_url(String baseUrl) {
        this.baseUrl = baseUrl;
        httpClient = new OkHttpClient.Builder()
                .dns(s -> {
                    startMoco(s, baseUrl);
                    return singletonList(InetAddress.getLocalHost());
                })
                .build();
    }

    @SneakyThrows
    private void startMoco(@NotNull String s, String baseUrl) {
        assertThat(new URL(baseUrl).getHost()).isEqualTo(s);
        requestedBaseUrl = baseUrl;
        (runner = runner(httpServer(80, requestHit = requestHit()))).start();
    }

    @SneakyThrows
    @When("GET {string}")
    public void get(String path) {
        httpClient.newCall(new Request.Builder()
                .url(baseUrl + path)
                .build()).execute();
    }

    @Then("{string} got a GET request on {string}")
    public void got_a_get_request_on(String url, String path) {
        requestHit.verify(and(by(uri(path)), by(method("GET"))), once());
        assertThat(url).isEqualTo(requestedBaseUrl);
    }

    @After
    void stopMoco() {
        if (runner != null)
            runner.stop();
    }
}
