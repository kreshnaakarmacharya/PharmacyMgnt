CREATE TABLE sale_details (
                              sale_detail_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              medicine_id VARCHAR(50) NOT NULL,
                              medicine_name VARCHAR(100) NOT NULL,
                              quantity INT NOT NULL,
                              unit_price DOUBLE NOT NULL,
                              total_price DOUBLE NOT NULL,
                              sale_id BIGINT,
                              CONSTRAINT fk_sale
                                  FOREIGN KEY (sale_id) REFERENCES sales(sale_id)
                                      ON DELETE CASCADE
);
