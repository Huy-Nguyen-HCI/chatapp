package controllers.api

import javax.inject.Inject
import play.api.mvc._
import play.api.libs.json.JsValue

import models.Friendship
import dao.{FriendshipDao, UserDao}
import controllers.USERNAME_KEY

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by thang on 6/22/17.
  */
class FriendshipAPI @Inject() (friendshipDao: FriendshipDao, userDao: UserDao)
                              (implicit executionContext: ExecutionContext)
                    extends Controller {

  // the json object keys for API request
  val SENDER_KEY = "sender"
  val RECEIVER_KEY = "receiver"


  def sendFriendRequest = Authenticated {
    Action.async(parse.json) { request =>

      val (senderId, receiverId) = getUserIdPair(request.body)

      for {
        s <- senderId
        r <- receiverId
        friendship <- friendshipDao.checkFriendship(s, r)
      } yield {
        // check if the sender is blocked by the receiver
        if (friendship.isDefined && friendship.get == (Friendship.STATUS_BLOCKED, r))
          BadRequest("Cannot send friend request")
        else {
          friendshipDao.insertOrUpdateFriendship(s, r, Friendship.STATUS_PENDING)
          Ok("Request sent")
        }
      }
    }
  }


  def acceptFriendRequest = Authenticated {
    Action.async(parse.json) { request =>

      val (senderId, receiverId) = getUserIdPair(request.body)

      for {
        s <- senderId
        r <- receiverId
        friendship <- friendshipDao.checkFriendship(s, r)
      } yield {
        // check if there is actually a pending friend request from the receiver
        if (friendship.isDefined && friendship.get == (Friendship.STATUS_PENDING, r)) {
          friendshipDao.insertOrUpdateFriendship(s, r, Friendship.STATUS_ACCEPTED)
          Ok("You are now friends!")
        } else BadRequest("There is no friend request")
      }
    }
  }

  /*
   * Action composition that authenticates HTTP request
   */
  case class Authenticated(action: Action[JsValue]) extends Action[JsValue] {

    def apply(request: Request[JsValue]): Future[Result] = {
      val userInfo = request.session.get(USERNAME_KEY)
      val sender = (request.body \ SENDER_KEY).as[String]

      if (userInfo.isDefined && sender == userInfo.get)
        action(request)
      else
        Future.successful(Unauthorized(views.html.defaultpages.unauthorized()))
    }

    lazy val parser: BodyParser[JsValue] = parse.json
  }

  // helper function to query the pair of (sender, receiver) from database
  private def getUserIdPair(json: JsValue) = {
    val senderName = (json \ SENDER_KEY).as[String]
    val receiverName = (json \ RECEIVER_KEY).as[String]
    val senderId = userDao.findByUsername(senderName).map(res => res.get.id.get)
    val receiverId = userDao.findByUsername(receiverName).map(res => res.get.id.get)

    (senderId, receiverId)
  }
}
