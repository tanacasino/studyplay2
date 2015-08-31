# --- !Ups
CREATE TABLE IF NOT EXISTS users (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `is_admin` TINYINT(1) NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE INDEX `name_UNIQUE` (`name` ASC)
) ENGINE = InnoDB;


# --- !Downs
DROP TABLE users IF EXISTS;

