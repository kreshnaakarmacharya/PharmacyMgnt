CREATE TABLE notifications (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               customer_id BIGINT NOT NULL,
                               order_id BIGINT,
                               title VARCHAR(100),
                               message TEXT,
                               type VARCHAR(30),        -- ORDER, PAYMENT
                               is_read BOOLEAN DEFAULT FALSE,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
