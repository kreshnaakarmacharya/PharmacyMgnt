Create Table sales(
                      sale_id BIGINT AUTO_INCREMENT,
                      customer_name VARCHAR(200) NOT NULL,
                      customer_address VARCHAR(200) NOT NULL,
                      customer_conatct VARCHAR(200) NOT NULL,
                      sale_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      sub_total DECIMAL(10,2),
                      discount DECIMAL(10,2),
                      net_total DECIMAL(10,2),
                      tender_amount DECIMAL(10,2),
                      returned_amount DECIMAL(10,2),
                      PRIMARY KEY(sale_id)
);