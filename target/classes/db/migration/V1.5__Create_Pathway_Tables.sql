-- Enterprise-Grade Pathway Management Database Schema
-- Migration: V1.5__Create_Pathway_Tables.sql

-- =====================================================
-- PATHWAY MANAGEMENT TABLES
-- =====================================================

-- Main pathways table
CREATE TABLE pathways (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    organization_id BIGINT NOT NULL,
    completion_badge_id BIGINT,
    completion_type VARCHAR(50) DEFAULT 'CONJUNCTION',
    status VARCHAR(20) DEFAULT 'DRAFT',
    version VARCHAR(20) DEFAULT '1.0.0',
    is_template BOOLEAN DEFAULT FALSE,
    template_category VARCHAR(100),
    estimated_duration_hours INT,
    difficulty_level VARCHAR(20),
    tags JSON,
    metadata JSON,
    created_by BIGINT NOT NULL,
    published_at TIMESTAMP NULL,
    published_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (organization_id) REFERENCES organizations(id),
    FOREIGN KEY (completion_badge_id) REFERENCES badge_classes(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (published_by) REFERENCES users(id),
    INDEX idx_organization_status (organization_id, status),
    INDEX idx_created_by (created_by),
    INDEX idx_status (status),
    INDEX idx_template (is_template)
);

-- Pathway elements table
CREATE TABLE pathway_elements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pathway_id BIGINT NOT NULL,
    parent_element_id BIGINT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    short_code VARCHAR(50),
    element_type VARCHAR(50) DEFAULT 'ELEMENT',
    order_index INT DEFAULT 0,
    completion_rule VARCHAR(50) DEFAULT 'ALL',
    required_count INT DEFAULT 1,
    is_optional BOOLEAN DEFAULT FALSE,
    counts_towards_parent BOOLEAN DEFAULT TRUE,
    estimated_duration_hours DOUBLE,
    difficulty_level VARCHAR(20),
    prerequisites JSON,
    competencies JSON,
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (pathway_id) REFERENCES pathways(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_element_id) REFERENCES pathway_elements(id) ON DELETE CASCADE,
    INDEX idx_pathway_order (pathway_id, order_index),
    INDEX idx_parent (parent_element_id),
    INDEX idx_element_type (element_type)
);

-- Pathway element badges table
CREATE TABLE pathway_element_badges (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    element_id BIGINT NOT NULL,
    badge_class_id BIGINT NOT NULL,
    badge_source VARCHAR(50) DEFAULT 'BADGR',
    external_badge_url VARCHAR(500),
    external_badge_data JSON,
    is_required BOOLEAN DEFAULT TRUE,
    verified_by BIGINT,
    verified_at TIMESTAMP NULL,
    verification_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (element_id) REFERENCES pathway_elements(id) ON DELETE CASCADE,
    FOREIGN KEY (badge_class_id) REFERENCES badge_classes(id),
    FOREIGN KEY (verified_by) REFERENCES users(id),
    UNIQUE KEY unique_element_badge (element_id, badge_class_id),
    INDEX idx_badge_source (badge_source),
    INDEX idx_is_required (is_required)
);

-- Pathway progress table
CREATE TABLE pathway_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pathway_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    progress_percentage DOUBLE DEFAULT 0.00,
    completed_elements INT DEFAULT 0,
    total_elements INT DEFAULT 0,
    completed_badges INT DEFAULT 0,
    total_badges INT DEFAULT 0,
    is_completed BOOLEAN DEFAULT FALSE,
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    completion_badge_issued BOOLEAN DEFAULT FALSE,
    completion_badge_issued_at TIMESTAMP NULL,
    last_activity_at TIMESTAMP NULL,
    time_spent_minutes INT DEFAULT 0,
    achievements JSON,
    notes TEXT,
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (pathway_id) REFERENCES pathways(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY unique_pathway_user (pathway_id, user_id),
    INDEX idx_user_progress (user_id, is_completed),
    INDEX idx_pathway_progress (pathway_id, is_completed),
    INDEX idx_completed (is_completed)
);

-- Pathway element progress table
CREATE TABLE pathway_element_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pathway_progress_id BIGINT NOT NULL,
    element_id BIGINT NOT NULL,
    is_completed BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP NULL,
    completed_badges JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (pathway_progress_id) REFERENCES pathway_progress(id) ON DELETE CASCADE,
    FOREIGN KEY (element_id) REFERENCES pathway_elements(id),
    UNIQUE KEY unique_progress_element (pathway_progress_id, element_id),
    INDEX idx_element_completed (element_id, is_completed)
);

-- =====================================================
-- ORGANIZATION API INTEGRATION TABLES
-- =====================================================

-- Organization API configuration table
CREATE TABLE organization_api_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    api_name VARCHAR(100) NOT NULL,
    api_type VARCHAR(50) NOT NULL,
    base_url VARCHAR(500) NOT NULL,
    api_key VARCHAR(255),
    api_secret VARCHAR(255),
    access_token VARCHAR(1000),
    refresh_token VARCHAR(1000),
    token_expires_at TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT FALSE,
    last_sync_at TIMESTAMP NULL,
    sync_frequency_minutes INT DEFAULT 30,
    webhook_url VARCHAR(500),
    webhook_secret VARCHAR(255),
    api_settings JSON,
    sync_config JSON,
    error_count INT DEFAULT 0,
    last_error_message TEXT,
    last_error_at TIMESTAMP NULL,
    success_count INT DEFAULT 0,
    last_success_at TIMESTAMP NULL,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    INDEX idx_organization_active (organization_id, is_active),
    INDEX idx_api_type (api_type),
    INDEX idx_is_verified (is_verified)
);

-- =====================================================
-- ENTERPRISE METRICS TABLES
-- =====================================================

-- Pathway analytics table
CREATE TABLE pathway_analytics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pathway_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    total_enrollments INT DEFAULT 0,
    total_completions INT DEFAULT 0,
    average_completion_time_hours DOUBLE,
    completion_rate DOUBLE,
    average_progress_percentage DOUBLE,
    most_popular_elements JSON,
    least_popular_elements JSON,
    analytics_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pathway_id) REFERENCES pathways(id) ON DELETE CASCADE,
    FOREIGN KEY (organization_id) REFERENCES organizations(id),
    UNIQUE KEY unique_pathway_date (pathway_id, analytics_date),
    INDEX idx_organization_date (organization_id, analytics_date),
    INDEX idx_analytics_date (analytics_date)
);

-- API sync history table
CREATE TABLE api_sync_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    sync_type VARCHAR(50) NOT NULL,
    sync_status VARCHAR(20) NOT NULL,
    sync_message TEXT,
    records_processed INT DEFAULT 0,
    records_successful INT DEFAULT 0,
    records_failed INT DEFAULT 0,
    sync_duration_ms BIGINT,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    INDEX idx_organization_sync (organization_id, sync_type),
    INDEX idx_sync_status (sync_status),
    INDEX idx_started_at (started_at)
);

-- =====================================================
-- ENTERPRISE FEATURES TABLES
-- =====================================================

-- Pathway templates table
CREATE TABLE pathway_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    difficulty_level VARCHAR(20),
    estimated_duration_hours INT,
    tags JSON,
    template_data JSON NOT NULL,
    is_public BOOLEAN DEFAULT FALSE,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_category (category),
    INDEX idx_difficulty (difficulty_level),
    INDEX idx_is_public (is_public)
);

-- Pathway endorsements table
CREATE TABLE pathway_endorsements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pathway_id BIGINT NOT NULL,
    endorser_organization_id BIGINT NOT NULL,
    endorsement_text TEXT,
    endorsement_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pathway_id) REFERENCES pathways(id) ON DELETE CASCADE,
    FOREIGN KEY (endorser_organization_id) REFERENCES organizations(id),
    UNIQUE KEY unique_pathway_endorser (pathway_id, endorser_organization_id),
    INDEX idx_endorser_organization (endorser_organization_id),
    INDEX idx_is_active (is_active)
);

-- =====================================================
-- DATA INTEGRITY CONSTRAINTS
-- =====================================================

-- Add check constraints for data integrity
ALTER TABLE pathways 
ADD CONSTRAINT chk_pathway_status 
CHECK (status IN ('DRAFT', 'REVIEW', 'PUBLISHED', 'ARCHIVED', 'DEPRECATED'));

ALTER TABLE pathways 
ADD CONSTRAINT chk_completion_type 
CHECK (completion_type IN ('CONJUNCTION', 'DISJUNCTION', 'SEQUENCE', 'WEIGHTED'));

ALTER TABLE pathway_elements 
ADD CONSTRAINT chk_element_type 
CHECK (element_type IN ('ELEMENT', 'GROUP', 'REQUIREMENT', 'MILESTONE'));

ALTER TABLE pathway_elements 
ADD CONSTRAINT chk_completion_rule 
CHECK (completion_rule IN ('ALL', 'SOME', 'EITHER', 'SEQUENCE', 'WEIGHTED'));

ALTER TABLE organization_api_configs 
ADD CONSTRAINT chk_api_type 
CHECK (api_type IN ('CANVAS', 'MOODLE', 'BLACKBOARD', 'D2L', 'SAKAI', 'CUSTOM', 'BADGR', 'ACCLAIM', 'OPEN_BADGES'));

-- =====================================================
-- SAMPLE DATA FOR TESTING
-- =====================================================

-- Insert sample pathway template
INSERT INTO pathway_templates (name, description, category, difficulty_level, estimated_duration_hours, tags, template_data, is_public, created_by) 
VALUES (
    'Basic Programming Pathway',
    'A foundational pathway for learning programming concepts',
    'Programming',
    'BEGINNER',
    40,
    '["programming", "coding", "beginner"]',
    '{"elements": [{"name": "Programming Fundamentals", "type": "ELEMENT"}, {"name": "Data Structures", "type": "ELEMENT"}, {"name": "Algorithms", "type": "ELEMENT"}]}',
    TRUE,
    1
);

-- Insert sample pathway endorsement
INSERT INTO pathway_endorsements (pathway_id, endorser_organization_id, endorsement_text, endorsement_date) 
VALUES (
    1,
    1,
    'This pathway provides excellent foundational knowledge for programming beginners.',
    CURDATE()
); 