package api

import play.api.mvc._
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json

import dao.{ChatRoomDao, UserDao}
import controllers.USERNAME_KEY

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChatRoomAPI @Inject()(userDao: UserDao, chatRoomDao: ChatRoomDao)(implicit ec: ExecutionContext)
  extends Controller {

  /**
    * List all participants in a given room. The user who made the request
    * must be logged in. That user must also be a participant in the room.
    */
  def listParticipants(roomId: Long) = Action.async { request =>
    request.session.get(USERNAME_KEY) match {
      case Some(username) =>
        for {
          idList <- chatRoomDao.getAllParticipantIds(roomId)
          userList <- userDao.listByIds(idList)
        } yield {
          val usernameList = userList.map(_.username)
          if (usernameList contains username)
            Ok(Json.toJson(usernameList))
          else
            Forbidden("This room is not accessible to you.")
        }

      case None =>
        Future.successful(Unauthorized(views.html.defaultpages.unauthorized()))
    }
  }


  /**
    * Add a new participant to the room with this id.
    * Only the owner of the room can add participant.
    */
  def addParticipant(roomId: Long, participantName: String) = Action.async { request =>
    request.session.get(USERNAME_KEY) match {
      case Some(username) =>
        val userQuery = userDao.findByUsername(username) // get the connected user
        val participantQuery = userDao.findByUsername(participantName) // get the participant
        val ownerIdQuery = chatRoomDao.getOwnerId(roomId) // get the room owner's id

        val result = for {
          user <- userQuery
          participant <- participantQuery
          ownerId <- ownerIdQuery
        } yield (user, participant, ownerId)

        result.flatMap {
          case (Some(user), Some(participant), ownerId) =>
            // only the owner can add participant
            if (user.id.get == ownerId)
              chatRoomDao.addParticipant(roomId, participant.id.get).map(_ => Ok("Successfully add participant."))
            else Future.successful(Unauthorized("You are not allowed to add participant."))

          case _ =>
            Future.successful(BadRequest("Some users do not exist"))
        }

      case None =>
        Future.successful(Unauthorized(views.html.defaultpages.unauthorized()))
    }
  }


  def createRoom = Action.async { request =>
    request.session.get(USERNAME_KEY) match {
      case Some(username) =>
        userDao.findByUsername(username).flatMap {
          case Some(user) =>
            chatRoomDao.insertRoom(user.id.get).map(roomId => Ok(Json.obj("roomId" -> roomId)))
          case None =>
            Future.successful(BadRequest("This user does not exist"))
        }
      case None =>
        Future.successful(Unauthorized(views.html.defaultpages.unauthorized()))
    }
  }


  def listAccessibleRoomIds = Action.async { request =>
    request.session.get(USERNAME_KEY) match {
      case Some(username) =>
        userDao.findByUsername(username).flatMap {
          case Some(user) => chatRoomDao.getAccessibleRoomIds(user.id.get).map(res => Ok(Json.toJson(res)))
          case None => Future.successful(BadRequest("This user does not exist"))
        }
      case None =>
        Future.successful(Unauthorized(views.html.defaultpages.unauthorized()))
    }
  }
}
