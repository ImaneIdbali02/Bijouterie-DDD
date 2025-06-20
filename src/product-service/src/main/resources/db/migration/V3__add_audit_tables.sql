-- Migration V3: Création des tables d'audit et de logs
-- Date: 2024-01-01
-- Description: Tables pour l'audit et le suivi des modifications

-- ========================================
-- TABLE: product_audit_logs
-- ========================================
CREATE TABLE product_audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL, -- CREATE, UPDATE, DELETE, ACTIVATE, DEACTIVATE
    user_id UUID,
    user_email VARCHAR(255),
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Index pour product_audit_logs
CREATE INDEX idx_product_audit_logs_product_id ON product_audit_logs(product_id);
CREATE INDEX idx_product_audit_logs_action ON product_audit_logs(action);
CREATE INDEX idx_product_audit_logs_created_at ON product_audit_logs(created_at);
CREATE INDEX idx_product_audit_logs_user_id ON product_audit_logs(user_id);

-- ========================================
-- TABLE: category_audit_logs
-- ========================================
CREATE TABLE category_audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    user_id UUID,
    user_email VARCHAR(255),
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

-- Index pour category_audit_logs
CREATE INDEX idx_category_audit_logs_category_id ON category_audit_logs(category_id);
CREATE INDEX idx_category_audit_logs_action ON category_audit_logs(action);
CREATE INDEX idx_category_audit_logs_created_at ON category_audit_logs(created_at);

-- ========================================
-- TABLE: collection_audit_logs
-- ========================================
CREATE TABLE collection_audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    collection_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    user_id UUID,
    user_email VARCHAR(255),
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (collection_id) REFERENCES collections(id) ON DELETE CASCADE
);

-- Index pour collection_audit_logs
CREATE INDEX idx_collection_audit_logs_collection_id ON collection_audit_logs(collection_id);
CREATE INDEX idx_collection_audit_logs_action ON collection_audit_logs(action);
CREATE INDEX idx_collection_audit_logs_created_at ON collection_audit_logs(created_at);

-- ========================================
-- TABLE: system_logs
-- ========================================
CREATE TABLE system_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    level VARCHAR(10) NOT NULL, -- INFO, WARN, ERROR, DEBUG
    logger_name VARCHAR(255),
    message TEXT NOT NULL,
    stack_trace TEXT,
    thread_name VARCHAR(100),
    user_id UUID,
    session_id VARCHAR(255),
    ip_address INET,
    user_agent TEXT,
    request_url VARCHAR(500),
    request_method VARCHAR(10),
    response_status INTEGER,
    execution_time_ms INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index pour system_logs
CREATE INDEX idx_system_logs_level ON system_logs(level);
CREATE INDEX idx_system_logs_created_at ON system_logs(created_at);
CREATE INDEX idx_system_logs_user_id ON system_logs(user_id);
CREATE INDEX idx_system_logs_logger_name ON system_logs(logger_name);

-- Contraintes pour les niveaux de log
ALTER TABLE system_logs 
ADD CONSTRAINT chk_system_logs_level 
CHECK (level IN ('INFO', 'WARN', 'ERROR', 'DEBUG'));

ALTER TABLE product_audit_logs 
ADD CONSTRAINT chk_product_audit_action 
CHECK (action IN ('CREATE', 'UPDATE', 'DELETE', 'ACTIVATE', 'DEACTIVATE', 'PRICE_CHANGE', 'STOCK_UPDATE'));

ALTER TABLE category_audit_logs 
ADD CONSTRAINT chk_category_audit_action 
CHECK (action IN ('CREATE', 'UPDATE', 'DELETE', 'ACTIVATE', 'DEACTIVATE', 'MOVE'));

ALTER TABLE collection_audit_logs 
ADD CONSTRAINT chk_collection_audit_action 
CHECK (action IN ('CREATE', 'UPDATE', 'DELETE', 'PUBLISH', 'ARCHIVE', 'ADD_PRODUCT', 'REMOVE_PRODUCT')); 