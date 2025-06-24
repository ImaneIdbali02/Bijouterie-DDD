package com.enaya.product_service.infrastructure.persistence.elasticsearch.service;

import com.enaya.product_service.domain.model.product.Product;

import java.util.List;

public interface ElasticsearchSyncService {
    
    void indexProduct(Product product);
    
    void deleteProduct(String productId);
    
    void bulkIndexProducts(List<Product> products);
} 