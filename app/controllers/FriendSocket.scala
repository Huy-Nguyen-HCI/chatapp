package controllers

import javax.inject.Inject

import akka.actor._
import akka.stream.Materializer
import play.api.mvc._
import play.api.libs.streams._

/**
  * Created by thangle on 7/20/17.
  */
class FriendSocket @Inject() (implicit system: ActorSystem, materializer: Materializer) {

  def socket = WebSocket.accept[String, String] { implicit request =>
    ActorFlow.actorRef(out => MyWebSocketActor.props(out))
  }
}

object MyWebSocketActor {
  def props(out: ActorRef) = Props(new MyWebSocketActor(out))
}

class MyWebSocketActor(out: ActorRef) extends Actor {
  def receive = {
    case msg: String =>
      out ! msg
  }
}