package com.enaya.product_service.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;

@Configuration
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUrl;

    @Value("${spring.elasticsearch.username}")
    private String username;

    @Value("${spring.elasticsearch.password}")
    private String password;

    @Value("${spring.elasticsearch.connection-timeout}")
    private String connectionTimeout;

    @Value("${spring.elasticsearch.socket-timeout}")
    private String socketTimeout;

    @Bean
    public RestClient restClient() throws NoSuchAlgorithmException {
        // Create the low-level client
        BasicCredentialsProvider credsProv = new BasicCredentialsProvider();
        credsProv.setCredentials(
            AuthScope.ANY,
            new UsernamePasswordCredentials(username, password)
        );

        return RestClient.builder(HttpHost.create(elasticsearchUrl))
            .setHttpClientConfigCallback(httpClientBuilder -> {
                httpClientBuilder.setDefaultCredentialsProvider(credsProv);
                try {
                    httpClientBuilder.setSSLContext(SSLContext.getDefault());
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
                return httpClientBuilder;
            })
            .setRequestConfigCallback(requestConfigBuilder -> {
                requestConfigBuilder.setConnectTimeout(Integer.parseInt(connectionTimeout.replace("s", "")) * 1000);
                requestConfigBuilder.setSocketTimeout(Integer.parseInt(socketTimeout.replace("s", "")) * 1000);
                return requestConfigBuilder;
            })
            .build();
    }

    @Bean
    public ElasticsearchTransport elasticsearchTransport() throws NoSuchAlgorithmException {
        return new RestClientTransport(
            restClient(),
            new JacksonJsonpMapper()
        );
    }

    @Bean
    public ElasticsearchClient elasticsearchClient() throws NoSuchAlgorithmException {
        return new ElasticsearchClient(elasticsearchTransport());
    }
}