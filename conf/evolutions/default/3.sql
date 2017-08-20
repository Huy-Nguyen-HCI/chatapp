# --- !Ups
CREATE TABLE chat_room (
  id INTEGER,
  owner_id INTEGER NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY(owner_id) REFERENCES user(id) ON DELETE CASCADE
);

CREATE TABLE chat_room_participant (
  room_id INTEGER,
  user_id INTEGER,
  PRIMARY KEY (room_id, user_id),
  FOREIGN KEY (room_id) REFERENCES chat_room(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

# --- !Downs
DROP TABLE chat_room;
DROP TABLE chat_room_participant;
