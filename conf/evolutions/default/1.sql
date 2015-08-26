# --- !Ups

create table users (
  id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  age INT NOT NULL,
  PRIMARY KEY (id)
);


# --- !Downs

drop table users if exists;
