CREATE TABLE prescription (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              customer_id BIGINT NOT NULL,
                              file_name VARCHAR(255) NOT NULL,
                              file_type VARCHAR(50) NOT NULL,
                              file_path VARCHAR(500) NOT NULL,
                              uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP
);