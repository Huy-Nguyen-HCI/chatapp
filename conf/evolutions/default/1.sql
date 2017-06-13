# --- !Ups
CREATE TABLE user (
  'username' VARCHAR NOT NULL PRIMARY KEY,
  'password' VARCHAR NOT NULL,
  'email' VARCHAR
);

CREATE UNIQUE INDEX account_index ON user('email');

# --- !Downs
DROP TABLE user;
