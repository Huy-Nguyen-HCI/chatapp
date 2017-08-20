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

  private val chatRoomsParticipants = TableQuery[ChatRoomsParticipantsTable]

  /** Returns the id of the person who created this room. */
  def getOwnerId(id: Long): Future[Option[Long]] =
    db.run(chatRooms.filter(_.id === id).map(_.ownerId).result.headOption)

  /** Inserts a new room with the user with this id as the owner. */
  def insert(ownerId: Long): Future[Long] = {
    val action = for {
      roomId <- (chatRooms returning chatRooms.map(_.id)) += ChatRoom(None, ownerId)
      _ <- chatRoomsParticipants += ChatRoomParticipant(roomId, ownerId)
    } yield roomId

    db.run(action)
  }

  /** Adds a new participant to a given room with this id. */
  def addParticipant(roomId: Long, userId: Long): Future[Unit] =
    db.run(chatRoomsParticipants += ChatRoomParticipant(roomId, userId)).map(_ => ())

  /** Returns the ids of all participants in the room with this id. */
  def getAllParticipantIds(roomId: Long): Future[Seq[Long]] =
    db.run(chatRoomsParticipants.filter(_.roomId === roomId).map(_.userId).result)
}

