# --- !Ups
CREATE TABLE friendship (
  'id1' INTEGER,
  'id2' INTEGER,
  'status' INT,
  'action_id' INT,
  FOREIGN KEY('id1') REFERENCES user('id'),
  FOREIGN KEY('id2') REFERENCES user('id')
);

CREATE UNIQUE INDEX friendship_index ON friendship ('id1', 'id2');


# --- !Downs
DROP TABLE friendship;
