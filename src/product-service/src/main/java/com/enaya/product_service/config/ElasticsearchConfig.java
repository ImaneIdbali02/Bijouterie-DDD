package com.enaya.product_service.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;

@Configuration
public class ElasticsearchConfig {

    @Value("${elasticsearch.host:localhost}")
    private String host;

    @Value("${elasticsearch.port:9200}")
    private int port;

    @Value("${elasticsearch.scheme:http}")
    private String scheme;

    @Value("${elasticsearch.username:elastic}")
    private String username;

    @Value("${elasticsearch.password:changeme}")
    private String password;

    @Value("${elasticsearch.enabled:true}")
    private boolean elasticsearchEnabled;

    @Bean
    public ObjectMapper elasticsearchObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    @ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = true)
    public RestClient restClient() throws NoSuchAlgorithmException {
        BasicCredentialsProvider credsProv = new BasicCredentialsProvider();
        credsProv.setCredentials(
            AuthScope.ANY,
            new UsernamePasswordCredentials(username, password)
        );

        return RestClient.builder(new HttpHost(host, port, scheme))
            .setHttpClientConfigCallback(httpClientBuilder -> {
                httpClientBuilder.setDefaultCredentialsProvider(credsProv);
                try {
                    httpClientBuilder.setSSLContext(SSLContext.getDefault());
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
                return httpClientBuilder;
            })
            .build();
    }

    @Bean
    @ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = true)
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient, ObjectMapper elasticsearchObjectMapper) {
        JacksonJsonpMapper jsonpMapper = new JacksonJsonpMapper(elasticsearchObjectMapper);
        return new RestClientTransport(restClient, jsonpMapper);
    }

    @Bean
    @ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = true)
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }
}