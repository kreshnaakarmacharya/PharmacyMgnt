CREATE TABLE UserRegistration (
                                  id BIGINT AUTO_INCREMENT,
                                  full_name VARCHAR(200) NOT NULL,
                                  email VARCHAR(200) NOT NULL,
                                  phone_number VARCHAR(200) NOT NULL,
                                  address VARCHAR(200) NOT NULL,
                                  date_of_birth DATE NOT NULL,
                                  gender VARCHAR(200) NOT NULL,
                                  password VARCHAR(200) NOT NULL,
                                  PRIMARY KEY (id)
);