CREATE TABLE prescription_upload (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     customer_id BIGINT NOT NULL,
                                     medicine_id BIGINT NOT NULL,
                                     image_path VARCHAR(255),
                                     upload_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                                     STATUS ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING'
);