package actors

import akka.actor.{Actor, ActorRef}


/**
  * Created by thangle on 7/21/17.
  */
class Chat extends Actor {

  import Chat._

  def receive = process(Set.empty)

  def process (subscribers: Set[ActorRef]): Receive = {
    case Join =>
      context become process(subscribers + sender)

    case Leave =>
      context become process(subscribers - sender)

    case msg: ClientSentMessage =>
      (subscribers - sender).foreach(_ ! msg)
  }
}

object Chat {
  case object Join
  case object Leave
}