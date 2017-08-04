package actors

import akka.actor.{Actor, ActorRef}
import play.api.libs.json._


/**
  * Created by thangle on 7/25/17.
  */
class Notification extends Actor {

  import Notification._

  // map users to their actor references
  var actorRefs: Map[String, ActorRef] = Map()
  // map users to their offline messages from other users
  var offlineMessages: Map[String, Seq[String]] = Map()

  def receive = {
    case Join(name) =>
      actorRefs += (name -> sender)
      // check if there is any offline message
      if (offlineMessages contains name) {
        offlineMessages(name).foreach(msg => sender ! ClientSentMessage(msg))
        offlineMessages -= name
      }

    case Leave(name) =>
      actorRefs -= name

    case ClientSentMessage(text) =>
      val json = Json.parse(text)
      val receiver = (json \ WS_RECEIVER_KEY).as[String]
      // send the message to its receiver
      // if the receiver is not connecting, store the message in the list of offline messages
      actorRefs.get(receiver) match {
        case Some(actorRef) => actorRef ! ClientSentMessage(text)

        case None =>
          if (!offlineMessages.contains(receiver)) {
            offlineMessages += (receiver -> Seq.empty[String])
          }

          offlineMessages += (receiver -> (text +: offlineMessages(receiver)))
      }
  }
}

object Notification {
  final case class Join(name: String)
  final case class Leave(name: String)
}