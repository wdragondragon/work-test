package org.example.worktest.flink;

import org.apache.flink.client.program.rest.RestClusterClient;
import org.apache.flink.configuration.Configuration;

public class FlinkRestClientProvider implements AutoCloseable {

    private final RestClusterClient<String> client;

    public FlinkRestClientProvider(Configuration config) throws Exception {
        this.client = new RestClusterClient<>(config, "standalone");
    }

    public RestClusterClient<String> getClient() {
        return client;
    }

    @Override
    public void close() throws Exception {
        client.close();
    }
}
