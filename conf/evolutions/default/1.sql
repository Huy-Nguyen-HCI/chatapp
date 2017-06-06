# --- !Ups
CREATE TABLE users(
  'username' VARCHAR,
  'email' VARCHAR,
  'password' VARCHAR
);

CREATE UNIQUE INDEX account_index ON users('username', 'email');

INSERT INTO users('username', 'email', 'password') VALUES('pucca', 'axx.bx.c@gmail.com', 'pucca');

# --- !Downs
DROP TABLE users;
