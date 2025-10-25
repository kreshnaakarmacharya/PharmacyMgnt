CREATE TABLE medicinedetails (
                                 id BIGINT NOT NULL AUTO_INCREMENT,
                                 NAME VARCHAR(255),
                                 brand_name VARCHAR(255),
                                 category_name VARCHAR(255),
                                 dosage_form VARCHAR(255),
                                 strength VARCHAR(255),
                                 manufacturer VARCHAR(255),
                                 batch_number VARCHAR(255),
                                 manufacture_date DATE,
                                 expiry_date DATE,
                                 quantity_in_stock INT,
                                 price DOUBLE,
                                 DESCRIPTION TEXT,
                                 image_url VARCHAR(255),
                                 PRIMARY KEY (id)

);

