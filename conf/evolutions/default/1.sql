# --- !Ups

create table users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  age INT NOT NULL,
  PRIMARY KEY (id)
);


# --- !Downs

drop table users if exists;
