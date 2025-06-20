package com.enaya.product_service.domain.service;

import com.enaya.product_service.domain.model.product.Product;
import com.enaya.product_service.domain.model.product.ProductVariant;
import com.enaya.product_service.domain.model.product.valueobjects.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductValidationService {

    /**
     * Valide un produit et retourne une liste d'erreurs de validation
     */
    public List<String> validateProduct(Product product) {
        List<String> errors = new ArrayList<>();

        // Validation des informations de base
        validateBasicInfo(product, errors);

        // Validation des variantes
        validateVariants(product, errors);

        // Validation des attributs
        validateAttributes(product, errors);

        // Validation des images
        validateImages(product, errors);

        // Validation des relations
        validateRelations(product, errors);

        return errors;
    }

    private void validateBasicInfo(Product product, List<String> errors) {
        // Validation du nom
        if (product.getName() == null) {
            errors.add("Le nom du produit est obligatoire");
        }

        // Validation de la description
        if (product.getDescription() == null || product.getDescription().trim().isEmpty()) {
            errors.add("La description du produit est obligatoire");
        }

        // Validation du SKU
        if (product.getSku() == null) {
            errors.add("Le SKU du produit est obligatoire");
        }

        // Validation du prix
        if (product.getPrice() == null) {
            errors.add("Le prix du produit est obligatoire");
        } else {
            validatePrice(product.getPrice(), errors, "produit");
        }

        // Validation de la catégorie
        if (product.getCategoryId() == null) {
            errors.add("La catégorie du produit est obligatoire");
        }

        // Validation des collections
        if (product.getCollectionIds() == null || product.getCollectionIds().isEmpty()) {
            errors.add("Le produit doit appartenir à au moins une collection");
        }
    }

    private void validateVariants(Product product, List<String> errors) {
        if (product.getVariants() == null || product.getVariants().isEmpty()) {
            errors.add("Le produit doit avoir au moins une variante");
            return;
        }

        product.getVariants().forEach(variant -> {
            // Validation du nom
            if (variant.getName() == null) {
                errors.add("Le nom de la variante est obligatoire");
            }

            // Validation du SKU
            if (variant.getSku() == null) {
                errors.add("Le SKU de la variante est obligatoire");
            }

            // Validation du prix
            if (variant.getPrice() == null) {
                errors.add("Le prix de la variante est obligatoire");
            } else {
                validatePrice(variant.getPrice(), errors, "variante");
                // Vérification que le prix de la variante n'est pas inférieur au prix de base
                if (variant.getPrice().getAmount().compareTo(product.getPrice().getAmount()) < 0) {
                    errors.add("Le prix de la variante ne peut pas être inférieur au prix de base du produit");
                }
            }

            // Validation des dimensions
            if (variant.getDimensions() != null) {
                validateDimensions(variant.getDimensions(), errors);
            }

            // Validation des attributs spécifiques
            validateVariantAttributes(variant, errors);

            // Validation des images de la variante
            validateVariantImages(variant, errors);

            // Validation du statut de stock
            if (variant.getStockStatus() == null) {
                errors.add("Le statut de stock de la variante est obligatoire");
            }
        });
    }

    private void validateAttributes(Product product, List<String> errors) {
        if (product.getAttributes() == null || product.getAttributes().isEmpty()) {
            errors.add("Le produit doit avoir au moins un attribut");
            return;
        }

        product.getAttributes().forEach(attribute -> {
            if (attribute.getName() == null || attribute.getName().trim().isEmpty()) {
                errors.add("Le nom de l'attribut est obligatoire");
            }

            if (attribute.getValue() == null || attribute.getValue().trim().isEmpty()) {
                errors.add("La valeur de l'attribut est obligatoire");
            }
        });
    }

    private void validateVariantAttributes(ProductVariant variant, List<String> errors) {
        if (variant.getSpecificAttributes() != null) {
            variant.getSpecificAttributes().forEach(attribute -> {
                if (attribute.getName() == null || attribute.getName().trim().isEmpty()) {
                    errors.add("Le nom de l'attribut spécifique est obligatoire");
                }

                if (attribute.getValue() == null || attribute.getValue().trim().isEmpty()) {
                    errors.add("La valeur de l'attribut spécifique est obligatoire");
                }
            });
        }
    }

    private void validateImages(Product product, List<String> errors) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            errors.add("Le produit doit avoir au moins une image");
            return;
        }

        product.getImages().forEach(image -> {
            if (image.getUrl() == null || image.getUrl().trim().isEmpty()) {
                errors.add("L'URL de l'image est obligatoire");
            }

            if (image.getDisplayOrder() < 0) {
                errors.add("L'ordre d'affichage de l'image doit être positif");
            }
        });
    }

    private void validateVariantImages(ProductVariant variant, List<String> errors) {
        if (variant.getImages() != null) {
            variant.getImages().forEach(image -> {
                if (image.getUrl() == null || image.getUrl().trim().isEmpty()) {
                    errors.add("L'URL de l'image de la variante est obligatoire");
                }

                if (image.getDisplayOrder() < 0) {
                    errors.add("L'ordre d'affichage de l'image de la variante doit être positif");
                }
            });
        }
    }

    private void validatePrice(Price price, List<String> errors, String context) {
        if (price.getAmount() == null || price.getAmount().compareTo(BigDecimal.valueOf(0)) <= 0) {
            errors.add("Le prix du " + context + " doit être supérieur à zéro");
        }

        if (price.getCurrency() == null) {
            errors.add("La devise du " + context + " est obligatoire");
        }
    }

    private void validateDimensions(JewelryDimensions dimensions, List<String> errors) {
        if (dimensions.getLength() <= 0) {
            errors.add("La longueur doit être supérieure à zéro");
        }

        if (dimensions.getWidth() <= 0) {
            errors.add("La largeur doit être supérieure à zéro");
        }

        if (dimensions.getHeight() < 0) {
            errors.add("La hauteur ne peut pas être négative");
        }

        if (dimensions.getWeight() <= 0) {
            errors.add("Le poids doit être supérieur à zéro");
        }

        // Vérification du type de bijou
        if (!dimensions.isRing() && !dimensions.isNecklace() && 
            !dimensions.isBracelet() && !dimensions.isEarring()) {
            errors.add("Les dimensions ne correspondent à aucun type de bijou standard");
        }
    }

    private void validateRelations(Product product, List<String> errors) {
        // Validation de la catégorie
        if (product.getCategoryId() == null) {
            errors.add("L'ID de la catégorie est obligatoire");
        }

        // Validation des collections
        if (product.getCollectionIds() != null) {
            product.getCollectionIds().forEach(collectionId -> {
                if (collectionId == null) {
                    errors.add("L'ID de la collection est obligatoire");
                }
            });
        }
    }

    /**
     * Vérifie si un produit est valide pour la vente
     */
    public boolean isProductValidForSale(Product product) {
        List<String> errors = validateProduct(product);
        if (!errors.isEmpty()) {
            log.warn("Le produit n'est pas valide pour la vente: {}", errors);
            return false;
        }

        // Vérification supplémentaire pour la vente
        return product.isActive() && 
               product.getVariants().stream()
                      .anyMatch(ProductVariant::isInStock);
    }

    /**
     * Vérifie si un produit est en promotion
     */
    public boolean isProductOnPromotion(Product product) {
        return product.getVariants().stream()
                .anyMatch(variant -> 
                    variant.getPrice() != null &&
                    product.getPrice() != null &&
                    variant.getPrice().getAmount().compareTo(product.getPrice().getAmount()) < 0
                );
    }

    /**
     * Vérifie si un produit est en rupture de stock
     */
    public boolean isProductOutOfStock(Product product) {
        return product.getVariants().stream()
                .allMatch(variant -> 
                    variant.getStockStatus() == ProductVariant.StockStatus.OUT_OF_STOCK
                );
    }
}
