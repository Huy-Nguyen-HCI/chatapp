package actors

import akka.actor.{Actor, ActorRef}

/**
  * Created by thangle on 7/21/17.
  */
class ClientActor(out: ActorRef, chat: ActorRef) extends Actor {

  chat ! Join

  override def postStop() = chat ! Leave

  def receive = {
    case text: String =>
      chat ! ClientSentMessage(text)

    case ClientSentMessage(text) =>
      out ! text
  }
}
