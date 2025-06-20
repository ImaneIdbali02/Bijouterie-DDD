package com.enaya.product_service.infrastructure.persistence.elasticsearch.service;

import com.enaya.product_service.domain.model.product.Product;
import com.enaya.product_service.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSyncService {

    private final ProductRepository productRepository;
    private final ElasticsearchSyncService elasticsearchSyncService;

    @Scheduled(fixedRate = 3600000) // toutes les heures
    public void syncAllProducts() {
        log.info("Starting full product sync with Elasticsearch");
        try {
            List<Product> products = (List<Product>) productRepository.findAll();
            log.info("Found {} products to sync", products.size());
            elasticsearchSyncService.bulkIndexProducts(products);
            log.info("Completed full product sync");
        } catch (Exception e) {
            log.error("Error during full product sync: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedRate = 1800000) // toutes les 30 minutes
    public void syncRecentProducts() {
        log.info("Starting recent products sync with Elasticsearch");
        try {
            // Récupérer les produits modifiés dans les dernières 30 minutes
            List<Product> recentProducts = productRepository.findByCreatedAtAfter(
                java.time.LocalDateTime.now().minusMinutes(30)
            );
            log.info("Found {} recent products to sync", recentProducts.size());
            elasticsearchSyncService.bulkIndexProducts(recentProducts);
            log.info("Completed recent products sync");
        } catch (Exception e) {
            log.error("Error during recent products sync: {}", e.getMessage(), e);
        }
    }
} 