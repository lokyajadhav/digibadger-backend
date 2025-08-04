-- Pathway Implementation Database Schema
-- This script creates all necessary tables for the pathway system

-- 1. pathways table
CREATE TABLE IF NOT EXISTS pathways (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    organization_id BIGINT NOT NULL,
    completion_badge_id BIGINT,
    completion_type VARCHAR(50) DEFAULT 'conjunction',
    status VARCHAR(20) DEFAULT 'draft', -- draft, published, archived
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    FOREIGN KEY (organization_id) REFERENCES organizations(id),
    FOREIGN KEY (completion_badge_id) REFERENCES badge_classes(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_organization_status (organization_id, status),
    INDEX idx_created_by (created_by)
);

-- 2. pathway_elements table
CREATE TABLE IF NOT EXISTS pathway_elements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pathway_id BIGINT NOT NULL,
    parent_element_id BIGINT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    element_type VARCHAR(50) DEFAULT 'element', -- element, group, requirement
    order_index INT DEFAULT 0,
    completion_rule VARCHAR(50) DEFAULT 'all', -- all, some, either
    required_count INT DEFAULT 1,
    is_optional BOOLEAN DEFAULT FALSE,
    counts_towards_parent BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pathway_id) REFERENCES pathways(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_element_id) REFERENCES pathway_elements(id) ON DELETE CASCADE,
    INDEX idx_pathway_order (pathway_id, order_index),
    INDEX idx_parent (parent_element_id)
);

-- 3. pathway_element_badges table
CREATE TABLE IF NOT EXISTS pathway_element_badges (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    element_id BIGINT NOT NULL,
    badge_class_id BIGINT NOT NULL,
    badge_source VARCHAR(50) DEFAULT 'badgr', -- badgr, canvas, acclaim, external
    external_badge_url VARCHAR(500),
    external_badge_data JSON,
    is_required BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (element_id) REFERENCES pathway_elements(id) ON DELETE CASCADE,
    FOREIGN KEY (badge_class_id) REFERENCES badge_classes(id),
    UNIQUE KEY unique_element_badge (element_id, badge_class_id)
);

-- 4. recipient_groups table
CREATE TABLE IF NOT EXISTS recipient_groups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    organization_id BIGINT NOT NULL,
    pathway_id BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    FOREIGN KEY (organization_id) REFERENCES organizations(id),
    FOREIGN KEY (pathway_id) REFERENCES pathways(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_organization_pathway (organization_id, pathway_id)
);

-- 5. recipient_group_members table
CREATE TABLE IF NOT EXISTS recipient_group_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES recipient_groups(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY unique_group_user (group_id, user_id)
);

-- 6. pathway_progress table
CREATE TABLE IF NOT EXISTS pathway_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pathway_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    group_id BIGINT NULL,
    progress_percentage DECIMAL(5,2) DEFAULT 0.00,
    completed_elements INT DEFAULT 0,
    total_elements INT DEFAULT 0,
    is_completed BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP NULL,
    completion_badge_issued BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (pathway_id) REFERENCES pathways(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (group_id) REFERENCES recipient_groups(id) ON DELETE SET NULL,
    UNIQUE KEY unique_pathway_user (pathway_id, user_id),
    INDEX idx_user_progress (user_id, is_completed)
);

-- 7. pathway_element_progress table
CREATE TABLE IF NOT EXISTS pathway_element_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pathway_progress_id BIGINT NOT NULL,
    element_id BIGINT NOT NULL,
    is_completed BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP NULL,
    completed_badges JSON, -- Array of badge IDs that completed this element
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (pathway_progress_id) REFERENCES pathway_progress(id) ON DELETE CASCADE,
    FOREIGN KEY (element_id) REFERENCES pathway_elements(id),
    UNIQUE KEY unique_progress_element (pathway_progress_id, element_id)
);

-- Add some sample data for testing
INSERT INTO pathways (name, description, organization_id, completion_type, status, created_by) 
VALUES 
('Sample Pathway 1', 'A sample pathway for testing', 1, 'conjunction', 'draft', 1),
('Sample Pathway 2', 'Another sample pathway', 1, 'disjunction', 'draft', 1);

-- Add sample pathway elements
INSERT INTO pathway_elements (pathway_id, name, description, element_type, order_index, completion_rule, required_count) 
VALUES 
(1, 'Academic Skills', 'Core academic competencies', 'group', 1, 'all', 1),
(1, 'Soft Skills', 'Communication and teamwork', 'group', 2, 'some', 2),
(2, 'Technical Skills', 'Programming and development', 'element', 1, 'all', 1);

-- Add child elements
INSERT INTO pathway_elements (pathway_id, parent_element_id, name, description, element_type, order_index, completion_rule, required_count) 
VALUES 
(1, 1, 'Mathematics', 'Mathematical reasoning and problem solving', 'element', 1, 'all', 1),
(1, 1, 'Science', 'Scientific method and critical thinking', 'element', 2, 'all', 1),
(1, 2, 'Communication', 'Written and verbal communication skills', 'element', 1, 'all', 1),
(1, 2, 'Leadership', 'Team leadership and project management', 'element', 2, 'all', 1); 
