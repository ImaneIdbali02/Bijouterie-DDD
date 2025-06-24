package com.enaya.product_service.infrastructure.persistence.elasticsearch.service;

import com.enaya.product_service.domain.model.product.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "false")
public class NoOpElasticsearchSyncService implements ElasticsearchSyncService {

    @Override
    public void indexProduct(Product product) {
        log.debug("Elasticsearch is disabled - skipping indexing for product: {}", product.getId());
    }

    @Override
    public void deleteProduct(String productId) {
        log.debug("Elasticsearch is disabled - skipping deletion for product: {}", productId);
    }

    @Override
    public void bulkIndexProducts(List<Product> products) {
        log.debug("Elasticsearch is disabled - skipping bulk indexing for {} products", products.size());
    }
} 