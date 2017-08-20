# --- !Ups
CREATE TABLE friendship (
  id1 INTEGER NOT NULL,
  id2 INTEGER NOT NULL,
  status INT NOT NULL,
  action_id INT NOT NULL,
  FOREIGN KEY(id1) REFERENCES user(id) ON DELETE CASCADE ,
  FOREIGN KEY(id2) REFERENCES user(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX friendship_index ON friendship (id1, id2);


# --- !Downs
DROP TABLE friendship;
