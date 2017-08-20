package dao

import models.ChatRoomParticipant
import play.api.db.slick.HasDatabaseConfigProvider
import slick.driver.JdbcProfile


private[dao] trait ChatRoomParticipantComponents extends ChatRoomComponents {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import driver.api._

  protected val chatRooms: TableQuery[ChatRoomsTable] = TableQuery[ChatRoomsTable]

  protected class ChatRoomsParticipantsTable(tag: Tag)
    extends Table[ChatRoomParticipant](tag, "chat_room_participant") {

    // fields
    def roomId = column[Long]("room_id")
    def userId = column[Long]("user_id")

    // foreign keys
    def roomIdFk = foreignKey("room_id_fk", roomId, chatRooms)(_.id, onDelete = ForeignKeyAction.Cascade)
    def userIdFk = foreignKey("owner_id_fk", userId, users)(_.id, onDelete = ForeignKeyAction.Cascade)


    override def * = (roomId, userId) <> ((ChatRoomParticipant.apply _).tupled, ChatRoomParticipant.unapply)
  }
}

