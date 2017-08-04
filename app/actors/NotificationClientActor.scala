package actors

import akka.actor.{Actor, ActorRef}


/**
  * Created by thangle on 7/26/17.
  */
class NotificationClientActor(out: ActorRef, notification: ActorRef, name: String) extends Actor {

  import Notification._

  notification ! Join(name)

  override def postStop() = notification ! Leave(name)

  def receive = {
    case text: String =>
      notification ! ClientSentMessage(text)

    case ClientSentMessage(text) =>
      out ! text
  }
}
