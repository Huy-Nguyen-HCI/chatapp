package models

case class ChatRoom(id: Option[Long], ownerId: Long)
case class ChatRoomParticipant(roomId: Long, userId: Long)