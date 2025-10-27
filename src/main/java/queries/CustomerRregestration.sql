CREATE TABLE UserRegistration (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  full_name VARCHAR(200) NOT NULL,
                                  email VARCHAR(200) NOT NULL UNIQUE,
                                  phone_number VARCHAR(20) NOT NULL,
                                  address VARCHAR(200) NOT NULL,
                                  date_of_birth DATE NOT NULL,
                                  gender VARCHAR(20) NOT NULL,
                                  password VARCHAR(200) NOT NULL,
                                  verified BOOLEAN NOT NULL DEFAULT FALSE
);
