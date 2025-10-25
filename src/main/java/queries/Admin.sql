CREATE TABLE Admin(
                      id int AUTO_INCREMENT,
                      username VARCHAR(200) NOT NULL ,
                      password VARCHAR(100) NOT NULL ,
                      Primary Key (id)
);

INSERT INTO Admin
VALUES ("1","admin123","12345");