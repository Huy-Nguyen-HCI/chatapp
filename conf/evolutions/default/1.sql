# --- !Ups
PRAGMA foreign_keys = ON;

CREATE TABLE user (
  id INTEGER,
  username VARCHAR UNIQUE NOT NULL,
  password VARCHAR NOT NULL,
  email VARCHAR UNIQUE NOT NULL,
  PRIMARY KEY (id)
);

# --- !Downs
DROP TABLE user;
