package actors

import akka.actor.{Actor, ActorRef}

case object Join
case object Leave
final case class ClientSentMessage(text: String)

/**
  * Created by thangle on 7/21/17.
  */
class Chat extends Actor {

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
