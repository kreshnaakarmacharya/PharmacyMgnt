CREATE TABLE contact_us (
                            id BIGINT AUTO_INCREMENT,
                            name VARCHAR(200) NOT NULL,
                            email VARCHAR(200) NOT NULL,
                            phone VARCHAR(50),
                            message TEXT,
                            PRIMARY KEY (id)
);