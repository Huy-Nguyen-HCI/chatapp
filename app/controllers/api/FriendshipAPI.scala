package controllers.api

import javax.inject.Inject

import play.api.mvc._
import models.Friendship
import dao.{FriendshipDao, UserDao}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by thang on 6/22/17.
  */
class FriendshipAPI @Inject() (friendshipDao: FriendshipDao, userDao: UserDao)
                              (implicit executionContext: ExecutionContext)
                    extends Controller {


  def sendFriendRequest = Action.async(parse.json) { request =>

    val senderName = (request.body \ "sender").as[String]
    val receiverName = (request.body \ "receiver").as[String]
    val senderId = userDao.findByUsername(senderName).map(res => res.get.id.get)
    val receiverId = userDao.findByUsername(receiverName).map(res => res.get.id.get)

    for {
      s <- senderId
      r <- receiverId
      friendship <- friendshipDao.checkFriendship(s, r)
    } yield {
      // check if the sender is blocked by the receiver
      val (status, actionId) = friendship

      if (status == Friendship.STATUS_BLOCKED && actionId == r)
        BadRequest("Cannot send friend request")
      else {
        friendshipDao.insertOrUpdateFriendship(s, r, Friendship.STATUS_PENDING)
        Ok("Success")
      }
    }

  }
}
