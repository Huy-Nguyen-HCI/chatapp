package actors

import akka.actor.{Actor, ActorRef}

/**
  * Created by thangle on 7/21/17.
  */
class ClientActor(out: ActorRef, chat: ActorRef, name: String) extends Actor {

  import CentralActor._

  chat ! Join(name)

  override def postStop() = chat ! Leave(name)

  def receive = {
    case text: String =>
      chat ! ClientSentMessage(text)

    case ClientSentMessage(text) =>
      out ! text
  }
}
