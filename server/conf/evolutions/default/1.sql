# --- First database schema

# --- !Ups

set ignorecase true;

CREATE TABLE user (
  id                        BIGINT NOT NULL AUTO_INCREMENT,
  name                      VARCHAR(255) NOT NULL,
  address                   VARCHAR(1000) NOT NULL,
  designation               VARCHAR(255) NOT NULL,
  CONSTRAINT pk_user PRIMARY KEY (id))
;

# --- !Downs

drop table if exists user;
