package actors

import akka.actor.{Actor, ActorRef, ActorSystem}
import play.api.libs.json.Json


/**
  * Created by thangle on 7/21/17.
  */
class CentralActor extends Actor {

  import CentralActor._

  def receive = process(Map.empty)

  def process (subscribers: Map[String, ActorRef]): Receive = {
    case Join(name) =>
      context become process(subscribers + (name -> sender()))

    case Leave(name) =>
      context become process(subscribers - name)

    case ClientSentMessage(text) =>
      // assume that text is in correct JSON format
      val json = Json.parse(text)
      val senderName = (json \ SENDER_KEY).as[String]
      val receiverName = (json \ RECEIVER_KEY).asOpt[String]
      val roomName = (json \ ROOM_KEY).as[String]

      // If the receiver is in the message, then only send that message to him or her.
      // Otherwise, send to all the connected users.
      receiverName match {
        case Some(username) =>
          if (subscribers.contains(username))
            subscribers(username) ! ClientSentMessage(text)

        case None =>
          for ((_, ref) <- subscribers - senderName) ref ! ClientSentMessage(text)
      }
  }
}

object CentralActor {
  // constants that represent the keys in json message
  val SENDER_KEY = "sender"
  val RECEIVER_KEY = "receiver"
  val ROOM_KEY = "room"
  val CHAT_MSG = "chat-message"
  val FRIEND_MSG = "friend-request"

  case class Join(name: String)
  case class Leave(name: String)
  case class ClientSentMessage(text: String)
  case class ClientSentRequest(text: String)
}