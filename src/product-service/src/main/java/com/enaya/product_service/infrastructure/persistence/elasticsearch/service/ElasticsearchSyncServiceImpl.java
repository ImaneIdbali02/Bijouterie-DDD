package com.enaya.product_service.infrastructure.persistence.elasticsearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.enaya.product_service.domain.model.product.Product;
import com.enaya.product_service.infrastructure.persistence.elasticsearch.dto.ProductIndexDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = true)
public class ElasticsearchSyncServiceImpl implements ElasticsearchSyncService {

    private final ElasticsearchClient elasticsearchClient;
    private final ObjectMapper elasticsearchObjectMapper;
    private static final String PRODUCTS_INDEX = "products";
    private final Queue<SyncOperation> failedOperations = new ConcurrentLinkedQueue<>();

    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void indexProduct(Product product) {
        try {
            log.debug("Indexing product: {}", product.getId());
            
            ProductIndexDto productIndexDto = ProductIndexDto.fromProduct(product);
            
            IndexResponse response = elasticsearchClient.index(i -> i
                .index(PRODUCTS_INDEX)
                .id(product.getId().toString())
                .document(productIndexDto)
            );
            log.info("Product indexed successfully: {} with result: {}", product.getId(), response.result());
        } catch (Exception e) {
            log.error("Error indexing product {}: {}", product.getId(), e.getMessage());
            
            // Si c'est une erreur d'authentification ou de connexion, on ne relance pas l'exception
            if (e.getMessage().contains("missing authentication credentials") || 
                e.getMessage().contains("security_exception") ||
                e.getMessage().contains("Connection refused") ||
                e.getMessage().contains("No route to host")) {
                log.warn("Elasticsearch connection issue - product will be indexed later: {}", product.getId());
                failedOperations.offer(new SyncOperation(SyncOperationType.INDEX, product));
                return; // Ne pas relancer l'exception pour éviter l'échec de la création du produit
            }
            
            failedOperations.offer(new SyncOperation(SyncOperationType.INDEX, product));
            throw new RuntimeException("Failed to index product", e);
        }
    }

    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void deleteProduct(String productId) {
        try {
            log.debug("Deleting product from index: {}", productId);
            
            DeleteResponse response = elasticsearchClient.delete(d -> d
                .index(PRODUCTS_INDEX)
                .id(productId)
            );
            log.info("Product deleted from index successfully: {} with result: {}", productId, response.result());
        } catch (Exception e) {
            log.error("Error deleting product {} from index: {}", productId, e.getMessage(), e);
            failedOperations.offer(new SyncOperation(SyncOperationType.DELETE, productId));
            throw new RuntimeException("Failed to delete product", e);
        }
    }

    @Override
    public void bulkIndexProducts(List<Product> products) {
        if (products == null || products.isEmpty()) {
            log.warn("No products to bulk index");
            return;
        }
        
        try {
            log.info("Starting bulk indexing of {} products", products.size());
            
            BulkRequest.Builder br = new BulkRequest.Builder();
            products.forEach(product -> {
                try {
                    ProductIndexDto productIndexDto = ProductIndexDto.fromProduct(product);
                    
                    br.operations(op -> op
                        .index(idx -> idx
                            .index(PRODUCTS_INDEX)
                            .id(product.getId().toString())
                            .document(productIndexDto)
                        )
                    );
                } catch (Exception e) {
                    log.error("Error preparing product {} for bulk index: {}", product.getId(), e.getMessage());
                    failedOperations.offer(new SyncOperation(SyncOperationType.INDEX, product));
                }
            });

            BulkResponse response = elasticsearchClient.bulk(br.build());
            if (response.errors()) {
                log.error("Bulk indexing had failures: {}", response.items().stream()
                    .filter(item -> item.error() != null)
                    .map(item -> String.format("Item %s failed: %s", item.id(), item.error().reason()))
                    .toList());
            } else {
                log.info("Successfully bulk indexed {} products", products.size());
            }
        } catch (Exception e) {
            log.error("Error during bulk indexing: {}", e.getMessage(), e);
            products.forEach(product -> 
                failedOperations.offer(new SyncOperation(SyncOperationType.INDEX, product))
            );
        }
    }

    @Scheduled(fixedRate = 300000) // toutes les 5 minutes
    public void retryFailedOperations() {
        log.info("Starting retry of failed operations. Queue size: {}", failedOperations.size());
        while (!failedOperations.isEmpty()) {
            SyncOperation operation = failedOperations.poll();
            try {
                operation.execute(elasticsearchClient, elasticsearchObjectMapper);
                log.info("Successfully retried operation: {}", operation);
            } catch (Exception e) {
                log.error("Failed to retry operation {}: {}", operation, e.getMessage());
                failedOperations.offer(operation);
            }
        }
    }

    private enum SyncOperationType {
        INDEX, DELETE
    }

    private static class SyncOperation {
        private final SyncOperationType type;
        private final Object data;

        public SyncOperation(SyncOperationType type, Object data) {
            this.type = type;
            this.data = data;
        }

        public void execute(ElasticsearchClient client, ObjectMapper mapper) throws Exception {
            switch (type) {
                case INDEX:
                    Product product = (Product) data;
                    ProductIndexDto productIndexDto = ProductIndexDto.fromProduct(product);
                    client.index(i -> i
                        .index(PRODUCTS_INDEX)
                        .id(product.getId().toString())
                        .document(productIndexDto)
                    );
                    break;
                case DELETE:
                    String productId = (String) data;
                    client.delete(d -> d
                        .index(PRODUCTS_INDEX)
                        .id(productId)
                    );
                    break;
            }
        }

        @Override
        public String toString() {
            return String.format("SyncOperation{type=%s, data=%s}", type, data);
        }
    }
} 