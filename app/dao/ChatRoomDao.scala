package dao

import javax.inject.{Inject, Singleton}

import models.{ChatRoom, ChatRoomParticipant}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class ChatRoomDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
                            (implicit ec: ExecutionContext)
  extends ChatRoomParticipantComponents with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

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
}

