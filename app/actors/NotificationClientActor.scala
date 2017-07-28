package actors

import akka.actor.{Actor, ActorRef}


/**
  * Created by thangle on 7/26/17.
  */
class NotificationClientActor(out: ActorRef, notification: ActorRef, name: String) extends Actor {

  import Notification.Join

  notification ! Join(name)

  def receive = {
    case text: String =>
      notification ! ClientSentMessage(text)

    case ClientSentMessage(text) =>
      out ! text
  }
}
