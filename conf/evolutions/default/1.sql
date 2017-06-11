# --- !Ups
CREATE TABLE user (
  'username' VARCHAR NOT NULL PRIMARY KEY,
  'password' VARCHAR NOT NULL,
  'email' VARCHAR
);

CREATE UNIQUE INDEX account_index ON user('email');

INSERT INTO user('username', 'password', 'email') VALUES('pucca', 'pucca', 'axx.bx.c@gmail.com');

# --- !Downs
DROP TABLE user;
