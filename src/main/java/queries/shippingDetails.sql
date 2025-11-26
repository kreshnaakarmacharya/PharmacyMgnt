CREATE TABLE ShippingAddress (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 customer_id BIGINT NOT NULL,
                                 recipient_name VARCHAR(200) NOT NULL,
                                 recipient_phone_number VARCHAR(50) NOT NULL,
                                 address VARCHAR(255) NOT NULL,

                                 CONSTRAINT fk_customer
                                     FOREIGN KEY (customer_id) REFERENCES userregistration(id)
                                         ON DELETE CASCADE
);