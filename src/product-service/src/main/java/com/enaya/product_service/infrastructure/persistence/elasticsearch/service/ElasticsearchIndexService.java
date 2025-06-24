package com.enaya.product_service.infrastructure.persistence.elasticsearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchIndexService {

    private final ElasticsearchClient elasticsearchClient;

    @PostConstruct
    public void createIndices() {
        try {
            log.info("Starting Elasticsearch indices creation...");
            createProductIndex();
            createCategoryIndex();
            log.info("Elasticsearch indices creation completed successfully");
        } catch (Exception e) {
            log.error("Failed to create Elasticsearch indices", e);
            // Ne pas faire échouer le démarrage de l'application
        }
    }

    private void createProductIndex() throws IOException {
        try (InputStream mappingIs = getClass().getResourceAsStream("/elasticsearch/product-mapping.json")) {
            
            if (mappingIs == null) {
                throw new IOException("Could not find product-mapping.json");
            }

            String mapping = new String(mappingIs.readAllBytes(), StandardCharsets.UTF_8);
            
            log.debug("Product mapping: {}", mapping);
            
            boolean indexExists = elasticsearchClient.indices().exists(e -> e.index("products")).value();
            if (!indexExists) {
                log.info("Creating products index...");
                CreateIndexRequest request = CreateIndexRequest.of(r -> r
                    .index("products")
                    .withJson(new java.io.StringReader(mapping))
                );
                
                CreateIndexResponse response = elasticsearchClient.indices().create(request);
                if (response.acknowledged()) {
                    log.info("Successfully created products index");
                } else {
                    log.error("Failed to create products index - not acknowledged");
                }
            } else {
                log.info("Products index already exists");
            }
        } catch (Exception e) {
            log.error("Error creating product index", e);
            throw e;
        }
    }

    private void createCategoryIndex() throws IOException {
        try (InputStream mappingIs = getClass().getResourceAsStream("/elasticsearch/category-mapping.json")) {
            
            if (mappingIs == null) {
                throw new IOException("Could not find category-mapping.json");
            }

            String mapping = new String(mappingIs.readAllBytes(), StandardCharsets.UTF_8);
            
            boolean indexExists = elasticsearchClient.indices().exists(e -> e.index("categories")).value();
            if (!indexExists) {
                log.info("Creating categories index...");
                CreateIndexRequest request = CreateIndexRequest.of(r -> r
                    .index("categories")
                    .withJson(new java.io.StringReader(mapping))
                );
                
                CreateIndexResponse response = elasticsearchClient.indices().create(request);
                if (response.acknowledged()) {
                    log.info("Successfully created categories index");
                } else {
                    log.error("Failed to create categories index - not acknowledged");
                }
            } else {
                log.info("Categories index already exists");
            }
        } catch (Exception e) {
            log.error("Error creating category index", e);
            throw e;
        }
    }
} 