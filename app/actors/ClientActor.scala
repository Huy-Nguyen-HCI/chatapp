package actors

import akka.actor.{Actor, ActorRef}
import play.api.libs.json.Json

/**
  * Created by thangle on 7/21/17.
  */
class ClientActor(out: ActorRef, central: ActorRef, name: String) extends Actor {

  import CentralActor._

  central ! Join(name)

  override def postStop() = central ! Leave(name)

  def receive = {
    case text: String => {
      val json = Json.parse(text)
      val msgType = (json \ "type").asOpt[String]
      msgType match {
        case Some(CHAT_MSG) => central ! ClientSentMessage(text)
        case Some(FRIEND_MSG) => central ! ClientSentRequest(text)
      }
    }

    case ClientSentMessage(text) =>
      out ! text
  }
}
