package controllers.api

import javax.inject.Inject

import play.api.mvc._
import models.Friendship
import dao.{FriendshipDao, UserDao}
import play.api.libs.json.JsValue

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by thang on 6/22/17.
  */
class FriendshipAPI @Inject() (friendshipDao: FriendshipDao, userDao: UserDao)
                              (implicit executionContext: ExecutionContext)
                    extends Controller {


  // helper function to query the pair of (sender, receiver) from database
  private def getUserIdPair(json: JsValue) = {
    val senderName = (json \ "sender").as[String]
    val receiverName = (json \ "receiver").as[String]
    val senderId = userDao.findByUsername(senderName).map(res => res.get.id.get)
    val receiverId = userDao.findByUsername(receiverName).map(res => res.get.id.get)

    (senderId, receiverId)
  }


  def sendFriendRequest = Action.async(parse.json) { request =>

    val (senderId, receiverId) = getUserIdPair(request.body)

    for {
      s <- senderId
      r <- receiverId
      friendship <- friendshipDao.checkFriendship(s, r)
    } yield {
      // check if the sender is blocked by the receiver
      if (friendship.isDefined) {
        val (status, actionId) = friendship.get
        if (status == Friendship.STATUS_BLOCKED && actionId == r)
          BadRequest("Cannot send friend request")
      }
      friendshipDao.insertOrUpdateFriendship(s, r, Friendship.STATUS_PENDING)
      Ok("Success")
    }
  }


  def acceptFriendRequest = Action.async(parse.json) { request =>

    val (senderId, receiverId) = getUserIdPair(request.body)

    for {
      s <- senderId
      r <- receiverId
      _ <- friendshipDao.insertOrUpdateFriendship(s, r, Friendship.STATUS_ACCEPTED)
    } yield Ok("Success")
  }
}
