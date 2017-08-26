package dao

import javax.inject.{Inject, Singleton}

import models.{ChatRoom, ChatRoomParticipant}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class ChatRoomDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
                            (implicit ec: ExecutionContext)
  extends UserComponents with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._


  private val users = TableQuery[UsersTable]
  private val chatRooms = TableQuery[ChatRoomsTable]
  /**
    * The chat-room-participant table does not include row (roomId, userId) such
    * that the user is the owner of the room.
    * */
  private val chatRoomsParticipants = TableQuery[ChatRoomsParticipantsTable]


  /** Returns the id of the person who created this room. */
  def getOwnerId(roomId: Long): Future[Long] =
    db.run(chatRooms.filter(_.id === roomId).map(_.ownerId).result.head)


  /** Inserts a new room with the user with this id as the owner. */
  def insertRoom(ownerId: Long): Future[Long] =
    db.run(chatRooms returning chatRooms.map(_.id) += ChatRoom(None, ownerId))


  /** Adds a new participant to a given room with this id. */
  def addParticipant(roomId: Long, userId: Long): Future[Unit] =
    db.run(chatRoomsParticipants += ChatRoomParticipant(roomId, userId)).map(_ => ())


  /** Returns the ids of all participants (including the owner) in the room with this id. */
  def getAllParticipantIds(roomId: Long): Future[Seq[Long]] = {
    val ownerQuery = getOwnerId(roomId)
    val participantsQuery = db.run(chatRoomsParticipants.filter(_.roomId === roomId).map(_.userId).result)

    for {
      ownerId <- ownerQuery
      participantIds <- participantsQuery
    } yield ownerId +: participantIds
  }


  /** Returns the ids of all rooms that are accessible to the user with this id. */
  def getAccessibleRoomIds(userId: Long): Future[Seq[Long]] = {
    // all the rooms owned by this user
    val ownedRoomIdsQuery = db.run(chatRooms.filter(_.ownerId === userId).map(_.id).result)
    val accessibleRoomIdsQuery = db.run(chatRoomsParticipants.filter(_.userId === userId).map(_.roomId).result)

    for {
      ownedRoomIds <- ownedRoomIdsQuery
      accessibleRoomIds <- accessibleRoomIdsQuery
    } yield ownedRoomIds ++ accessibleRoomIds
  }


  class ChatRoomsTable(tag: Tag) extends Table[ChatRoom](tag, "chat_room") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def ownerId = column[Long]("owner_id")

    // foreign key
    def ownerIdFk = foreignKey("owner_id_fk", id, users)(_.id, onDelete = ForeignKeyAction.Cascade)

    override def * = (id.?, ownerId) <> (ChatRoom.tupled, ChatRoom.unapply)
  }

  class ChatRoomsParticipantsTable(tag: Tag)
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

