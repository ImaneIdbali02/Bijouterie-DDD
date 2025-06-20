package com.enaya.product_service.domain.service;

import com.enaya.product_service.domain.model.product.Product;
import com.enaya.product_service.domain.model.product.ProductVariant;
import com.enaya.product_service.domain.model.product.valueobjects.*;
import com.enaya.product_service.domain.repository.ProductRepository;
import com.enaya.product_service.domain.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductDomainService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductValidationService validationService;
    private static final String DEFAULT_CURRENCY = "MAD";

    /**
     * Crée un nouveau produit
     */
    public Product createProduct(String name, String description, String sku,
                                Price price, UUID categoryId) {
        validateProductCreation(name, sku, price, categoryId);
        Product product = Product.create(name, description, sku, price, categoryId);
        return productRepository.save(product);
    }

    /**
     * Clone un produit existant avec un nouveau SKU
     */
    public Product cloneProduct(Product sourceProduct, String newSku, String newName) {
        Product clonedProduct = Product.builder()
                .name(newName)
                .description(sourceProduct.getDescription())
                .sku(newSku)
                .price(sourceProduct.getPrice())
                .categoryId(sourceProduct.getCategoryId())
                .collectionIds(new ArrayList<>(sourceProduct.getCollectionIds()))
                .attributes(new ArrayList<>(sourceProduct.getAttributes()))
                .images(new ArrayList<>(sourceProduct.getImages()))
                .active(false) // Le clone est inactif par défaut
                .build();

        // Cloner les variantes
        sourceProduct.getVariants().forEach(sourceVariant -> {
            ProductVariant clonedVariant = ProductVariant.builder()
                    .name(sourceVariant.getName())
                    .sku(sourceVariant.getSku() + "_CLONE")
                    .price(sourceVariant.getPrice())
                    .dimensions(sourceVariant.getDimensions())
                    .specificAttributes(new ArrayList<>(sourceVariant.getSpecificAttributes()))
                    .images(new ArrayList<>(sourceVariant.getImages()))
                    .active(false)
                    .stockStatus(ProductVariant.StockStatus.OUT_OF_STOCK)
                    .stockQuantity(0)
                    .build();
            clonedProduct.addVariant(clonedVariant);
        });

        return productRepository.save(clonedProduct);
    }

    /**
     * Valide si un produit peut être supprimé
     */
    public boolean canDeleteProduct(Product product) {
        if (product == null) {
            return false;
        }

        // Un produit ne peut être supprimé s'il a des variantes actives
        boolean hasActiveVariants = product.getVariants().stream()
                .anyMatch(ProductVariant::isActive);

        return !hasActiveVariants;
    }

    /**
     * Calcule le prix de vente suggéré avec une marge
     */
    public Price calculateSuggestedPrice(Price costPrice, BigDecimal marginPercentage) {
        if (costPrice == null || marginPercentage == null) {
            throw new IllegalArgumentException("Cost price and margin percentage cannot be null");
        }

        if (marginPercentage.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Margin percentage cannot be negative");
        }

        BigDecimal multiplier = BigDecimal.ONE.add(marginPercentage.divide(BigDecimal.valueOf(100)));
        BigDecimal suggestedAmount = costPrice.getAmount().multiply(multiplier);

        return Price.of(suggestedAmount, costPrice.getCurrency());
    }

    /**
     * Valide la cohérence des variantes d'un produit
     */
    public void validateProductVariants(Product product) {
        if (product == null || !product.hasVariants()) {
            return;
        }

        List<ProductVariant> variants = product.getVariants();

        // Vérifier l'unicité des SKU des variantes
        Set<String> skus = new HashSet<>();
        for (ProductVariant variant : variants) {
            String skuValue = variant.getSku();
            if (!skus.add(skuValue)) {
                throw new IllegalStateException("Duplicate SKU found in variants: " + skuValue);
            }
        }

        // Vérifier que toutes les variantes ont la même devise (MAD)
        for (ProductVariant variant : variants) {
            if (!variant.getPrice().getCurrency().equals(DEFAULT_CURRENCY)) {
                throw new IllegalStateException("All variants must have MAD as currency");
            }
        }
    }

    /**
     * Trouve la variante la moins chère d'un produit
     */
    public Optional<ProductVariant> findCheapestVariant(Product product) {
        if (product == null || !product.hasVariants()) {
            return Optional.empty();
        }

        return product.getVariants().stream()
                .filter(ProductVariant::isActive)
                .min(Comparator.comparing(variant -> variant.getPrice().getAmount()));
    }

    /**
     * Trouve la variante la plus chère d'un produit
     */
    public Optional<ProductVariant> findMostExpensiveVariant(Product product) {
        if (product == null || !product.hasVariants()) {
            return Optional.empty();
        }

        return product.getVariants().stream()
                .filter(ProductVariant::isActive)
                .max(Comparator.comparing(variant -> variant.getPrice().getAmount()));
    }

    /**
     * Calcule la fourchette de prix d'un produit (min-max des variantes)
     */
    public PriceRange calculatePriceRange(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        if (!product.hasVariants()) {
            return new PriceRange(product.getPrice(), product.getPrice());
        }

        Optional<ProductVariant> cheapest = findCheapestVariant(product);
        Optional<ProductVariant> mostExpensive = findMostExpensiveVariant(product);

        Price minPrice = cheapest.map(ProductVariant::getPrice).orElse(product.getPrice());
        Price maxPrice = mostExpensive.map(ProductVariant::getPrice).orElse(product.getPrice());

        return new PriceRange(minPrice, maxPrice);
    }

    /**
     * Vérifie si un produit est disponible (a au moins une variante en stock)
     */
    public boolean isProductAvailable(Product product) {
        if (product == null || !product.isActive()) {
            return false;
        }

        return product.getVariants().stream()
                .anyMatch(ProductVariant::isInStock);
    }

    /**
     * Groupe les produits par catégorie
     */
    public Map<UUID, List<Product>> groupProductsByCategory(List<Product> products) {
        if (products == null) {
            return new HashMap<>();
        }

        return products.stream()
                .collect(Collectors.groupingBy(Product::getCategoryId));
    }

    /**
     * Filtre les produits par attribut
     */
    public List<Product> filterProductsByAttribute(List<Product> products, String attributeName, String attributeValue) {
        if (products == null || attributeName == null || attributeValue == null) {
            return new ArrayList<>();
        }

        return products.stream()
                .filter(product -> hasAttributeWithValue(product, attributeName, attributeValue))
                .collect(Collectors.toList());
    }

    /**
     * Calcule le poids total d'un produit (incluant ses variantes)
     */
    public double calculateTotalWeight(Product product) {
        if (product == null) {
            return 0.0;
        }

        double totalWeight = 0.0;

        // Poids des variantes ayant des dimensions
        for (ProductVariant variant : product.getVariants()) {
            if (variant.getDimensions() != null) {
                totalWeight += variant.getDimensions().getWeight();
            }
        }

        return totalWeight;
    }

    /**
     * Valide les dimensions d'un bijou selon le type
     */
    public void validateJewelryDimensions(JewelryDimensions dimensions) {
        if (dimensions == null) {
            return;
        }

        // Validation spécifique selon le type de bijou
        if (dimensions.isRing()) {
            validateRingDimensions(dimensions);
        } else if (dimensions.isNecklace()) {
            validateNecklaceDimensions(dimensions);
        } else if (dimensions.isBracelet()) {
            validateBraceletDimensions(dimensions);
        } else if (dimensions.isEarring()) {
            validateEarringDimensions(dimensions);
        }
    }

    /**
     * Met à jour les informations de base d'un produit
     */
    public Product updateProductBasicInfo(Product product, String newName, String newDescription) {
        product.updateBasicInfo(newName, newDescription);
        return productRepository.save(product);
    }

    /**
     * Met à jour le prix d'un produit
     */
    public Product updateProductPrice(Product product, Price newPrice) {
        product.updatePrice(newPrice);
        return productRepository.save(product);
    }

    /**
     * Ajoute une variante à un produit
     */
    public Product addVariantToProduct(Product product, ProductVariant variant) {
        // Vérifier l'unicité des SKU des variantes
        Set<String> skus = new HashSet<>();
        for (ProductVariant existingVariant : product.getVariants()) {
            String skuValue = existingVariant.getSku();
            if (!skus.add(skuValue)) {
                throw new IllegalStateException("Duplicate SKU found in variants: " + skuValue);
            }
        }
        
        if (!skus.add(variant.getSku())) {
            throw new IllegalStateException("Duplicate SKU found in new variant: " + variant.getSku());
        }

        product.addVariant(variant);
        return productRepository.save(product);
    }

    /**
     * Supprime une variante d'un produit
     */
    public Product removeVariantFromProduct(Product product, UUID variantId) {
        product.removeVariant(variantId);
        return productRepository.save(product);
    }

    /**
     * Met à jour les informations de base d'une variante
     */
    public ProductVariant updateVariantBasicInfo(ProductVariant variant, String newName) {
        variant.updateBasicInfo(newName);
        return variantRepository.save(variant);
    }

    /**
     * Met à jour le prix d'une variante
     */
    public ProductVariant updateVariantPrice(ProductVariant variant, Price newPrice) {
        variant.updatePrice(newPrice);
        return variantRepository.save(variant);
    }

    /**
     * Met à jour le statut de stock d'une variante
     */
    public ProductVariant updateVariantStockStatus(ProductVariant variant, ProductVariant.StockStatus status) {
        variant.updateStockStatus(status);
        return variantRepository.save(variant);
    }

    /**
     * Met à jour la quantité en stock d'une variante
     */
    public ProductVariant updateVariantStockQuantity(ProductVariant variant, Integer quantity) {
        variant.updateStockQuantity(quantity);
        return variantRepository.save(variant);
    }

    /**
     * Met à jour la note d'une variante
     */
    public ProductVariant updateVariantRating(ProductVariant variant, Double rating) {
        variant.updateRating(rating);
        return variantRepository.save(variant);
    }

    /**
     * Incrémente le nombre de reviews d'une variante
     */
    public ProductVariant incrementVariantReviewCount(ProductVariant variant) {
        variant.incrementReviewCount();
        return variantRepository.save(variant);
    }

    /**
     * Ajoute un attribut à un produit
     */
    public Product addAttributeToProduct(Product product, ProductAttribute attribute) {
        product.addAttribute(attribute);
        return productRepository.save(product);
    }

    /**
     * Ajoute une image à un produit
     */
    public Product addImageToProduct(Product product, ProductImage image) {
        product.addImage(image);
        return productRepository.save(product);
    }

    /**
     * Supprime une image d'un produit
     */
    public Product removeImageFromProduct(Product product, String imageUrl) {
        product.removeImage(imageUrl);
        return productRepository.save(product);
    }

    /**
     * Ajoute un produit à une collection
     */
    public Product addProductToCollection(Product product, UUID collectionId) {
        product.addToCollection(collectionId);
        return productRepository.save(product);
    }

    /**
     * Retire un produit d'une collection
     */
    public Product removeProductFromCollection(Product product, UUID collectionId) {
        product.removeFromCollection(collectionId);
        return productRepository.save(product);
    }

    /**
     * Active un produit
     */
    public Product activateProduct(Product product) {
        product.activate();
        return productRepository.save(product);
    }

    /**
     * Désactive un produit
     */
    public Product deactivateProduct(Product product) {
        product.deactivate();
        return productRepository.save(product);
    }

    /**
     * Change la catégorie d'un produit
     */
    public Product changeProductCategory(Product product, UUID newCategoryId) {
        product.changeCategory(newCategoryId);
        return productRepository.save(product);
    }

    /**
     * Active une variante
     */
    public ProductVariant activateVariant(ProductVariant variant) {
        variant.activate();
        return variantRepository.save(variant);
    }

    /**
     * Désactive une variante
     */
    public ProductVariant deactivateVariant(ProductVariant variant) {
        variant.deactivate();
        return variantRepository.save(variant);
    }

    /**
     * Ajoute un attribut spécifique à une variante
     */
    public ProductVariant addSpecificAttributeToVariant(ProductVariant variant, ProductAttribute attribute) {
        variant.addSpecificAttribute(attribute);
        return variantRepository.save(variant);
    }

    /**
     * Ajoute une image à une variante
     */
    public ProductVariant addImageToVariant(ProductVariant variant, ProductImage image) {
        variant.addImage(image);
        return variantRepository.save(variant);
    }

    /**
     * Supprime une image d'une variante
     */
    public ProductVariant removeImageFromVariant(ProductVariant variant, String imageUrl) {
        variant.removeImage(imageUrl);
        return variantRepository.save(variant);
    }

    /**
     * Valide et sauvegarde un produit
     */
    public Product validateAndSaveProduct(Product product) {
        List<String> errors = validationService.validateProduct(product);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Product validation failed: " + String.join(", ", errors));
        }
        return productRepository.save(product);
    }

    /**
     * Recherche des produits similaires
     */
    public List<Product> findSimilarProducts(Product product, int limit) {
        return productRepository.findSimilarProducts(product.getId(), limit);
    }

    /**
     * Recherche des produits liés
     */
    public List<Product> findRelatedProducts(Product product, int limit) {
        Set<ProductAttribute> attributes = new HashSet<>(product.getAttributes());
        return productRepository.findRelatedProducts(
                product.getCategoryId(),
                attributes,
                product.getId(),
                limit
        );
    }

    /**
     * Vérifie si un SKU existe déjà
     */
    public boolean isSkuExists(String sku) {
        return productRepository.existsBySku(sku);
    }

    /**
     * Vérifie si un SKU de variante existe déjà
     */
    public boolean isVariantSkuExists(String sku) {
        return variantRepository.existsBySku(sku);
    }

    /**
     * Récupère tous les produits actifs
     */
    public List<Product> getAllActiveProducts() {
        return productRepository.findActiveProducts();
    }

    /**
     * Récupère tous les produits d'une catégorie
     */
    public List<Product> getProductsByCategory(UUID categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    /**
     * Récupère tous les produits d'une collection
     */
    public List<Product> getProductsByCollection(UUID collectionId) {
        return productRepository.findByCollectionId(collectionId);
    }

    /**
     * Recherche des produits par nom
     */
    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContaining(name);
    }

    /**
     * Recherche des produits par gamme de prix
     */
    public List<Product> searchProductsByPriceRange(double minPrice, double maxPrice) {
        return productRepository.findByPriceRange(minPrice, maxPrice);
    }

    /**
     * Recherche des produits par attributs
     */
    public List<Product> searchProductsByAttributes(Map<String, String> attributes) {
        return productRepository.findByAttributes(attributes);
    }

    /**
     * Recherche des produits en stock
     */
    public List<Product> searchProductsInStock() {
        return productRepository.findByVariantsStockStatus(ProductVariant.StockStatus.IN_STOCK);
    }

    /**
     * Récupère les statistiques des produits
     */
    public Map<String, Object> getProductStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalProducts = productRepository.count();
        List<Product> activeProducts = productRepository.findActiveProducts();
        List<Product> inStockProducts = productRepository.findByVariantsStockStatus(ProductVariant.StockStatus.IN_STOCK);
        
        stats.put("totalProducts", totalProducts);
        stats.put("activeProducts", activeProducts.size());
        stats.put("inStockProducts", inStockProducts.size());
        stats.put("inactiveProducts", totalProducts - activeProducts.size());
        
        return stats;
    }

    /**
     * Valide les paramètres de création d'un produit
     */
    public void validateProductCreation(String name, String sku, Price price, UUID categoryId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("Product SKU cannot be null or empty");
        }
        if (price == null) {
            throw new IllegalArgumentException("Product price cannot be null");
        }
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }
        
        // Vérifier si le SKU existe déjà
        if (productRepository.existsBySku(sku)) {
            throw new IllegalArgumentException("Product with SKU " + sku + " already exists");
        }
    }

    // Méthodes privées

    private boolean hasAttributeWithValue(Product product, String attributeName, String attributeValue) {
        return product.getAttributes().stream()
                .anyMatch(attr -> attributeName.equalsIgnoreCase(attr.getName()) &&
                        attributeValue.equalsIgnoreCase(attr.getValue()));
    }

    private void validateRingDimensions(JewelryDimensions dimensions) {
        if (dimensions.getLength() < 10 || dimensions.getLength() > 30) {
            throw new IllegalArgumentException("Ring diameter must be between 10mm and 30mm");
        }
        if (dimensions.getWidth() < 1 || dimensions.getWidth() > 15) {
            throw new IllegalArgumentException("Ring width must be between 1mm and 15mm");
        }
    }

    private void validateNecklaceDimensions(JewelryDimensions dimensions) {
        if (dimensions.getLength() < 300 || dimensions.getLength() > 1000) {
            throw new IllegalArgumentException("Necklace length must be between 300mm and 1000mm");
        }
    }

    private void validateBraceletDimensions(JewelryDimensions dimensions) {
        if (dimensions.getLength() < 150 || dimensions.getLength() > 250) {
            throw new IllegalArgumentException("Bracelet length must be between 150mm and 250mm");
        }
    }

    private void validateEarringDimensions(JewelryDimensions dimensions) {
        if (dimensions.getLength() > 100) {
            throw new IllegalArgumentException("Earring length cannot exceed 100mm");
        }
    }

    // Classe interne pour représenter une fourchette de prix
    public static class PriceRange {
        private final Price minPrice;
        private final Price maxPrice;

        public PriceRange(Price minPrice, Price maxPrice) {
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
        }

        public Price getMinPrice() {
            return minPrice;
        }

        public Price getMaxPrice() {
            return maxPrice;
        }

        public boolean isSinglePrice() {
            return minPrice.getAmount().equals(maxPrice.getAmount());
        }

        public Price getPriceDifference() {
            return maxPrice.subtract(minPrice);
        }

        @Override
        public String toString() {
            if (isSinglePrice()) {
                return minPrice.toString();
            }
            return String.format("%s - %s", minPrice, maxPrice);
        }
    }
}