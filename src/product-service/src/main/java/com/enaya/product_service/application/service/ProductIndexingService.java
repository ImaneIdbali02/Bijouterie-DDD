package com.enaya.product_service.application.service;

import com.enaya.product_service.domain.model.product.Product;
import com.enaya.product_service.infrastructure.persistence.elasticsearch.repository.ProductElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductIndexingService {

    private final ProductElasticsearchRepository elasticsearchRepository;

    @Async
    @Transactional
    public void indexProduct(Product product) {
        try {
            log.info("Indexation du produit: {}", product.getId());
            elasticsearchRepository.save(product);
        } catch (Exception e) {
            log.error("Erreur lors de l'indexation du produit {}: {}", product.getId(), e.getMessage());
            throw new ProductIndexingException("Erreur lors de l'indexation du produit", e);
        }
    }

    @Async
    @Transactional
    public void indexProducts(List<Product> products) {
        try {
            log.info("Indexation de {} produits", products.size());
            elasticsearchRepository.saveAll(products);
        } catch (Exception e) {
            log.error("Erreur lors de l'indexation des produits: {}", e.getMessage());
            throw new ProductIndexingException("Erreur lors de l'indexation des produits", e);
        }
    }

    @Async
    @Transactional
    public void removeFromIndex(String productId) {
        try {
            log.info("Suppression du produit de l'index: {}", productId);
            elasticsearchRepository.deleteById(productId);
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du produit {} de l'index: {}", productId, e.getMessage());
            throw new ProductIndexingException("Erreur lors de la suppression du produit de l'index", e);
        }
    }

    @Async
    @Transactional
    public void reindexAll(List<Product> products) {
        try {
            log.info("Réindexation de tous les produits");
            elasticsearchRepository.deleteAll();
            elasticsearchRepository.saveAll(products);
        } catch (Exception e) {
            log.error("Erreur lors de la réindexation des produits: {}", e.getMessage());
            throw new ProductIndexingException("Erreur lors de la réindexation des produits", e);
        }
    }
} 