-- Create pathway tables
CREATE TABLE pathways (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    organization_id BIGINT NOT NULL,
    completion_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE TABLE pathway_elements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pathway_id BIGINT NOT NULL,
    badge_class_id BIGINT,
    element_type VARCHAR(50),
    order_index INT,
    name VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pathway_id) REFERENCES pathways(id),
    FOREIGN KEY (badge_class_id) REFERENCES badge_classes(id)
);

CREATE TABLE pathway_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pathway_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    progress_percentage DECIMAL(5,2) DEFAULT 0.00,
    completed_elements INT DEFAULT 0,
    total_elements INT DEFAULT 0,
    is_completed BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (pathway_id) REFERENCES pathways(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE recipient_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    organization_id BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE TABLE recipient_group_members (
    recipient_group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (recipient_group_id, user_id),
    FOREIGN KEY (recipient_group_id) REFERENCES recipient_groups(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE pathway_subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pathway_id BIGINT NOT NULL,
    recipient_group_id BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pathway_id) REFERENCES pathways(id),
    FOREIGN KEY (recipient_group_id) REFERENCES recipient_groups(id)
);

-- Create indexes for better performance
CREATE INDEX idx_pathways_organization ON pathways(organization_id);
CREATE INDEX idx_pathway_elements_pathway ON pathway_elements(pathway_id);
CREATE INDEX idx_pathway_progress_user_pathway ON pathway_progress(user_id, pathway_id);
CREATE INDEX idx_recipient_groups_organization ON recipient_groups(organization_id);
CREATE INDEX idx_pathway_subscriptions_pathway ON pathway_subscriptions(pathway_id); 