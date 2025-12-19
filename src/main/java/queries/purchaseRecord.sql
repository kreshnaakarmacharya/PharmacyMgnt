CREATE TABLE purchase_record (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                 customer_id BIGINT NOT NULL,
                                 shipping_address_id BIGINT NOT NULL,

                                 purchased_medicine JSON,

                                 required_prescription BOOLEAN NOT NULL DEFAULT FALSE,

                                 prescription_img VARCHAR(255),

                                 purchase_date_time DATETIME DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT fk_purchase_customer
                                     FOREIGN KEY (customer_id)
                                         REFERENCES userregistration(id),
                                 CONSTRAINT fk_purchase_shipping
                                     FOREIGN KEY (shipping_address_id)
                                         REFERENCES shipping_address(id)
);
