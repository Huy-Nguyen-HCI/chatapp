package dao

import models.ChatRoom
import play.api.db.slick.HasDatabaseConfigProvider
import slick.driver.JdbcProfile


private[dao] trait ChatRoomComponents extends UserComponents { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import driver.api._

  protected val users: TableQuery[UsersTable] = TableQuery[UsersTable]

  class ChatRoomsTable(tag: Tag) extends Table[ChatRoom](tag, "chat_room") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def ownerId = column[Long]("owner_id")

    // foreign key
    def ownerIdFk = foreignKey("owner_id_fk", id, users)(_.id, onDelete = ForeignKeyAction.Cascade)

    override def * = (id.?, ownerId) <> (ChatRoom.tupled, ChatRoom.unapply)
  }
}


