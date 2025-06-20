-- Migration V1: Création des tables de base
-- Date: 2024-01-01
-- Description: Création des tables principales pour le service produit

-- ========================================
-- TABLE: categories
-- ========================================
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    parent_id UUID,
    level INTEGER NOT NULL DEFAULT 0,
    path VARCHAR(500) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    display_order INTEGER DEFAULT 0,
    image_url VARCHAR(500),
    creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modification_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    slug VARCHAR(255),
    child_category_ids TEXT, -- Liste d'UUIDs séparés par des virgules
    full_path VARCHAR(1000),
    visible_in_menu BOOLEAN NOT NULL DEFAULT true,
    metadata JSONB -- Pour les métadonnées SEO
);

-- Index pour les catégories
CREATE INDEX idx_categories_parent_id ON categories(parent_id);
CREATE INDEX idx_categories_active ON categories(active);
CREATE INDEX idx_categories_slug ON categories(slug);
CREATE INDEX idx_categories_path ON categories(path);
CREATE INDEX idx_categories_display_order ON categories(display_order);

-- ========================================
-- TABLE: collections
-- ========================================
CREATE TABLE collections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    slug VARCHAR(255) UNIQUE,
    period_start_date TIMESTAMP,
    period_end_date TIMESTAMP,
    product_ids TEXT, -- Liste d'UUIDs séparés par des virgules
    active BOOLEAN NOT NULL DEFAULT true,
    published BOOLEAN NOT NULL DEFAULT false,
    priority INTEGER NOT NULL DEFAULT 0,
    meta_title VARCHAR(255),
    meta_description VARCHAR(500),
    creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modification_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    publication_date TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

-- Index pour les collections
CREATE INDEX idx_collections_active ON collections(active);
CREATE INDEX idx_collections_published ON collections(published);
CREATE INDEX idx_collections_slug ON collections(slug);
CREATE INDEX idx_collections_priority ON collections(priority);

-- ========================================
-- TABLE: products
-- ========================================
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sku VARCHAR(100) UNIQUE NOT NULL,
    price_amount DECIMAL(10,2) NOT NULL,
    price_currency VARCHAR(3) NOT NULL DEFAULT 'MAD',
    category_id UUID,
    active BOOLEAN NOT NULL DEFAULT true,
    creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modification_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- Index pour les produits
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_active ON products(active);
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_price_amount ON products(price_amount);
CREATE INDEX idx_products_creation_date ON products(creation_date);

-- ========================================
-- TABLE: product_variants
-- ========================================
CREATE TABLE product_variants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    sku VARCHAR(100) UNIQUE NOT NULL,
    price_amount DECIMAL(10,2) NOT NULL,
    price_currency VARCHAR(3) NOT NULL DEFAULT 'MAD',
    dimensions_length DECIMAL(8,2),
    dimensions_width DECIMAL(8,2),
    dimensions_height DECIMAL(8,2),
    dimensions_weight DECIMAL(8,2),
    dimensions_unit VARCHAR(10) DEFAULT 'mm',
    active BOOLEAN NOT NULL DEFAULT true,
    stock_status VARCHAR(20) NOT NULL DEFAULT 'IN_STOCK',
    stock_quantity INTEGER,
    rating DECIMAL(3,2),
    review_count INTEGER DEFAULT 0,
    creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modification_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Index pour les variantes
CREATE INDEX idx_product_variants_product_id ON product_variants(product_id);
CREATE INDEX idx_product_variants_sku ON product_variants(sku);
CREATE INDEX idx_product_variants_active ON product_variants(active);
CREATE INDEX idx_product_variants_stock_status ON product_variants(stock_status);
CREATE INDEX idx_product_variants_price_amount ON product_variants(price_amount);

-- ========================================
-- TABLES DE LIAISON
-- ========================================

-- Table de liaison: product_collections
CREATE TABLE product_collections (
    product_id UUID NOT NULL,
    collection_id UUID NOT NULL,
    PRIMARY KEY (product_id, collection_id),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (collection_id) REFERENCES collections(id) ON DELETE CASCADE
);

-- Index pour product_collections
CREATE INDEX idx_product_collections_product_id ON product_collections(product_id);
CREATE INDEX idx_product_collections_collection_id ON product_collections(collection_id);

-- Table de liaison: product_attributes
CREATE TABLE product_attributes (
    product_id UUID NOT NULL,
    attribute_name VARCHAR(100) NOT NULL,
    attribute_value TEXT NOT NULL,
    attribute_type VARCHAR(20) DEFAULT 'STRING',
    PRIMARY KEY (product_id, attribute_name),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Index pour product_attributes
CREATE INDEX idx_product_attributes_product_id ON product_attributes(product_id);
CREATE INDEX idx_product_attributes_name ON product_attributes(attribute_name);
CREATE INDEX idx_product_attributes_value ON product_attributes(attribute_value);

-- Table de liaison: product_images
CREATE TABLE product_images (
    product_id UUID NOT NULL,
    url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(255),
    display_order INTEGER DEFAULT 0,
    image_type VARCHAR(20) DEFAULT 'SECONDARY',
    width INTEGER,
    height INTEGER,
    PRIMARY KEY (product_id, url),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Index pour product_images
CREATE INDEX idx_product_images_product_id ON product_images(product_id);
CREATE INDEX idx_product_images_display_order ON product_images(display_order);

-- Table de liaison: product_variant_attributes
CREATE TABLE product_variant_attributes (
    variant_id UUID NOT NULL,
    attribute_name VARCHAR(100) NOT NULL,
    attribute_value TEXT NOT NULL,
    attribute_type VARCHAR(20) DEFAULT 'STRING',
    PRIMARY KEY (variant_id, attribute_name),
    FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE CASCADE
);

-- Index pour product_variant_attributes
CREATE INDEX idx_product_variant_attributes_variant_id ON product_variant_attributes(variant_id);
CREATE INDEX idx_product_variant_attributes_name ON product_variant_attributes(attribute_name);

-- Table de liaison: product_variant_images
CREATE TABLE product_variant_images (
    variant_id UUID NOT NULL,
    url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(255),
    display_order INTEGER DEFAULT 0,
    image_type VARCHAR(20) DEFAULT 'SECONDARY',
    width INTEGER,
    height INTEGER,
    PRIMARY KEY (variant_id, url),
    FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE CASCADE
);

-- Index pour product_variant_images
CREATE INDEX idx_product_variant_images_variant_id ON product_variant_images(variant_id);
CREATE INDEX idx_product_variant_images_display_order ON product_variant_images(display_order);

-- Table de liaison: collection_images
CREATE TABLE collection_images (
    collection_id UUID NOT NULL,
    url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(255),
    display_order INTEGER DEFAULT 0,
    image_type VARCHAR(20) DEFAULT 'THUMBNAIL',
    file_size_bytes BIGINT,
    width INTEGER,
    height INTEGER,
    description TEXT,
    PRIMARY KEY (collection_id, url),
    FOREIGN KEY (collection_id) REFERENCES collections(id) ON DELETE CASCADE
);

-- Index pour collection_images
CREATE INDEX idx_collection_images_collection_id ON collection_images(collection_id);
CREATE INDEX idx_collection_images_display_order ON collection_images(display_order); 