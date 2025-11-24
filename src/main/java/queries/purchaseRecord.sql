CREATE TABLE purchase_record (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 customer_id BIGINT NOT NULL,
                                 purchased_medicine JSON,
                                 required_prescription BOOLEAN NOT NULL DEFAULT FALSE,
                                 prescription_img VARCHAR(255),
                                 purchaseDateTime DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 FOREIGN KEY (customer_id) REFERENCES userregistration(id)
);