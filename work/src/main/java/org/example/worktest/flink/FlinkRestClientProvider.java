package org.example.worktest.flink;

import org.apache.flink.client.program.rest.RestClusterClient;
import org.apache.flink.configuration.Configuration;
import org.example.worktest.flink.config.FlinkProperties;

public class FlinkRestClientProvider implements AutoCloseable {

    private final RestClusterClient<String> client;

    public FlinkRestClientProvider(Configuration config) throws Exception {
        this(config, "standalone");
    }

    public FlinkRestClientProvider(Configuration config, String clusterId) throws Exception {
        this.client = new RestClusterClient<>(config, clusterId);
    }

    public FlinkRestClientProvider(FlinkProperties flinkProperties) throws Exception {
        Configuration config = new Configuration();
        config.set(org.apache.flink.configuration.RestOptions.ADDRESS, flinkProperties.getRest().getAddress());
        config.set(org.apache.flink.configuration.RestOptions.PORT, flinkProperties.getRest().getPort());
        this.client = new RestClusterClient<>(config, flinkProperties.getRest().getClusterId());
    }

    public RestClusterClient<String> getClient() {
        return client;
    }

    @Override
    public void close() throws Exception {
        client.close();
    }
}
