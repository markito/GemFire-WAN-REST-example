package io.pivotal.example.dispatcher;

import com.gemstone.gemfire.pdx.JSONFormatter;
import com.gemstone.gemfire.pdx.PdxInstance;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.*;
import org.apache.http.protocol.HttpRequestExecutor;

import java.io.IOException;
import java.net.HttpRetryException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Uses Apache <code>HttpClient</code> to send REST calls to remote GemFire endpoint
 *
 * @author wmarkito
 */
public class HttpDispatcher {

    private static final Logger LOGGER = Logger.getLogger(HttpDispatcher.class.getCanonicalName());

    // TODO: system property
    private final static int RETRY_INTERVAL = 10 * 1000; // 10s
    private final static int MAX_RETRY = 3;
    private final static int TIMEOUT = 10 * 1000; // 10s

    private String ENDPOINT;
    CloseableHttpClient httpClient;

    public HttpDispatcher() {
        // change properly
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(TIMEOUT)
                .setSocketTimeout(TIMEOUT)
                .build();

        this.httpClient = HttpClients.custom()
                .setRequestExecutor(new HttpRequestExecutor())
                .setConnectionReuseStrategy(DefaultConnectionReuseStrategy.INSTANCE)
                .setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE)
                .disableAutomaticRetries()
                .setDefaultRequestConfig(config)
                .build();
    }


    /**
     * Create JSON list from <code>pdxInstanceList</code> using <code>JSONFormatter</code>
     * @param pdxInstanceList
     * @return String json list
     */
    public String toJSONList(final List<PdxInstance> pdxInstanceList) {
        final StringBuilder values = new StringBuilder();
        final String separator = ",";
        String loopDelim = "";

        values.append("[");

        for (PdxInstance entry : pdxInstanceList) {
            values.append(loopDelim).append(JSONFormatter.toJSON(entry));
            loopDelim = separator;
        }

        values.append("]");

        return values.toString();
    }

    /**
     * Send PdxInstance list to a GemFire RESTful endpoint
     * @param payload
     * @param keys
     * @return
     * @throws IOException
     */
    public boolean send(final String keys, final String payload) throws IOException {
        boolean isPutSuccessful = false;
        final HttpPut httpPut = new HttpPut(getEndpoint() + keys); // create method url with keys
        int statusCode = 0;

        try {
            HttpEntity entity = EntityBuilder.create()
                    .setText(payload)
                    .setContentType(ContentType.APPLICATION_JSON)
                    .setContentEncoding("gzip")
                    .gzipCompress()
                    .build();

            httpPut.setEntity(entity);

            int retryCount = 0;
            /**
             * try sending request <code>MAX_RETRY</code> times with <code>RETRY_INTERVAL</code> interval
             */
            do {
                try (CloseableHttpResponse putResponse = httpClient.execute(httpPut)) {
                    statusCode = putResponse.getStatusLine().getStatusCode();

                    if (statusCode == HttpStatus.SC_OK) {
                        isPutSuccessful = true;
                    } else {
                        retryCount++;
                        LOGGER.warning(String.format("HTTP request failed (status %s)- Retry in %d ms %d/%d", statusCode, RETRY_INTERVAL, retryCount, MAX_RETRY));

                        if (retryCount < MAX_RETRY) {
                            waitBeforeRetry(RETRY_INTERVAL);
                        }
                    }
                }
            } while ((!isPutSuccessful) && (retryCount < MAX_RETRY));

            if (retryCount >= MAX_RETRY) {
                throw new HttpRetryException("HTTP request failed after all attempts", statusCode, httpPut.getURI().toString());
            }

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, String.format("Error code: %d %n URI: %s %n Method: %s"
                    , statusCode, httpPut.getRequestLine().getUri(), httpPut.getRequestLine().getMethod()), ex);
            // rethrow exception
            throw ex;

        } finally {
            // always release connection
            httpPut.releaseConnection();
        }

        return isPutSuccessful;
    }

    private void waitBeforeRetry(final int retryInterval) {
        try {
            Thread.sleep(retryInterval);
        } catch (InterruptedException e) {
        }
    }

    private String getEndpoint() {
        return ENDPOINT;
    }

    public void setEndpoint(String ENDPOINT) {
        this.ENDPOINT = ENDPOINT.endsWith("/") ? ENDPOINT : ENDPOINT + "/";
    }

}
