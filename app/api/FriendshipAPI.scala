package api

import javax.inject.Inject

import dao.{FriendshipDao, UserDao}
import models.Friendship
import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}
import controllers.USERNAME_KEY

/**
  * Created by thang on 6/22/17.
  */
class FriendshipAPI @Inject() (friendshipDao: FriendshipDao, userDao: UserDao)
                              (implicit executionContext: ExecutionContext)
                    extends Controller {

  // the json object keys for API request
  val SENDER_KEY = "sender"
  val RECEIVER_KEY = "receiver"


  def sendFriendRequest = PostAuthenticated {
    Action.async(parse.json) { request =>

      val (senderId, receiverId) = getUserIdPair(request.body)

      for {
        s <- senderId
        r <- receiverId
        friendship <- friendshipDao.getFriendship(s, r)
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


  def acceptFriendRequest = PostAuthenticated {
    Action.async(parse.json) { request =>

      val (senderId, receiverId) = getUserIdPair(request.body)

      for {
        s <- senderId
        r <- receiverId
        friendship <- friendshipDao.getFriendship(s, r)
      } yield {
        // check if there is actually a pending friend request from the receiver
        if (friendship.isDefined && friendship.get == (Friendship.STATUS_PENDING, r)) {
          friendshipDao.insertOrUpdateFriendship(s, r, Friendship.STATUS_ACCEPTED)
          Ok("You are now friends!")
        } else BadRequest("There is no friend request")
      }
    }
  }


  def listUsersMakingStatus(username: String, status: Int) = SessionAuthenticated(username) {
    Action.async {
      // find the user in the database
      val query = userDao.findByUsername(username)

      query flatMap {
        case Some(user) =>
          friendshipDao.listUsersMakingStatus(user.id.get, status).flatMap(ids => {
            // get the user names by ids
            val names = userDao.listByIds(ids)
            names.map(res => Ok(Json.toJson(res.map(_.username))))
          })
        case None =>
          Future.successful(BadRequest("This user does not exist"))
      }
    }
  }

  // Get the current relationship between a user and another user, including the status and
  // the user who did the last action. The requested user must be logged in.
  def getStatus(requestedUsername: String, otherUsername: String) = SessionAuthenticated(requestedUsername) {
    Action.async {
      val requestedUserId = userDao.findByUsername(requestedUsername).map(res => res.get.id.get)
      val otherUserId = userDao.findByUsername(otherUsername).map(res => res.get.id.get)

      for {
        r <- requestedUserId
        o <- otherUserId
        friendship <- friendshipDao.getFriendship(r, o)
      } yield {
        if (friendship.isDefined) {
          // get the user with the actionID
          val actionId = friendship.get._2
          val actionUser = if (actionId == r) requestedUsername else otherUsername
          Ok(Json.parse(s""" {"status": ${friendship.get._1}, "actionUser": "$actionUser"} """))
        }
        else
          Ok(Json.obj())
      }
    }
  }


  /*
   * Action composition that authenticates the login user.
   */
  case class SessionAuthenticated[A](username: String)(action: Action[A]) extends Action[A] {

    def apply(request: Request[A]): Future[Result] = {
      val connectedUser = request.session.get(USERNAME_KEY)

      if (connectedUser.isDefined && username == connectedUser.get)
        action(request)
      else
        Future.successful(Unauthorized(views.html.defaultpages.unauthorized()))
    }

    lazy val parser: BodyParser[A] = action.parser
  }


  /*
   * Action composition that authenticates HTTP POST request
   */
  case class PostAuthenticated(action: Action[JsValue]) extends Action[JsValue] {

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
