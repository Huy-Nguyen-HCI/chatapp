# --- !Ups
CREATE TABLE friendship (
  'username1' VARCHAR,
  'username2' VARCHAR,
  'status' INT,
  'action_id' INT,
  FOREIGN KEY('username1') REFERENCES user('username'),
  FOREIGN KEY('username2') REFERENCES user('username'),
)

--- status code:
---   0: pending
---   1: accepted
---   2: declined
---   3: blocked

# --- !Downs
DROP TABLE friendship;
