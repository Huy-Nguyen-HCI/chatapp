package actors

import akka.actor.{Actor, ActorRef}

/**
  * Created by thangle on 7/21/17.
  */
class ClientActor(out: ActorRef, central: ActorRef, name: String) extends Actor {

  import CentralActor._

  central ! Join(name)

  override def postStop() = central ! Leave(name)

  def receive = {
    case text: String =>
      central ! ClientSentMessage(text)

    case ClientSentMessage(text) =>
      out ! text
  }
}
