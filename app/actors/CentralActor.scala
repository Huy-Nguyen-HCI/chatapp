package actors

import akka.actor.{Actor, ActorRef}
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
      val receivers = (json \ RECEIVER_KEY).as[List[String]]

      for (r <- receivers) {
        if (subscribers contains r) {
          subscribers(r) ! ClientSentMessage(text)
        }
      }
  }
}

object CentralActor {
  // constants that represent the keys in json message
  val RECEIVER_KEY = "receivers"

  case class Join(name: String)
  case class Leave(name: String)
  case class ClientSentMessage(text: String)
}