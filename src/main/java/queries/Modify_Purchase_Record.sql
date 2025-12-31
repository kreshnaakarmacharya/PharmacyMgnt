-- Patch to modify the purchase record

ALTER TABLE pharmacymanagement.purchase_record ADD COLUMN transaction_id VARCHAR(150);
ALTER TABLE pharmacymanagement.purchase_record ADD UNIQUE (transaction_id);
ALTER TABLE pharmacymanagement.purchase_record ADD COLUMN is_paid BOOLEAN DEFAULT FALSE;


CREATE TABLE pharmacymanagement.epay_status(
    id BIGINT UNSIGNED AUTO_INCREMENT,
    purchase_record_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    transaction_uuid VARCHAR(200),
    product_code VARCHAR(100),
    total_amount float DEFAULT 0,
    ref_id VARCHAR(100),
    PRIMARY KEY(id),
    FOREIGN KEY(purchase_record_id) REFERENCES purchase_record(id)
);