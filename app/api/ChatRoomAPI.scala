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
  def listParticipants(roomId: Long): Action[AnyContent] = Action.async { request =>
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


  def createRoom = Action.async { request =>
    request.session.get(USERNAME_KEY) match {
      case Some(username) =>
        userDao.findByUsername(username).flatMap {
          case Some(user) =>
            chatRoomDao.insertRoom(user.id.get).map(roomId => Ok(Json.obj("roomId" -> roomId)))
          case None => Future.successful(BadRequest("This user does not exist"))
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
