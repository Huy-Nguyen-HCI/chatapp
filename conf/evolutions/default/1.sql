# --- !Ups
CREATE TABLE user (
  'id' INTEGER PRIMARY KEY AUTOINCREMENT,
  'username' VARCHAR UNIQUE NOT NULL,
  'password' VARCHAR UNIQUE NOT NULL,
  'email' VARCHAR
);

# --- !Downs
DROP TABLE user;
