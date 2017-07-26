package controllers

import javax.inject.{Inject, Singleton}

import akka.actor.{ActorSystem, Props}
import play.api.mvc._
import actors._
import akka.stream.Materializer
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.streams.ActorFlow

@Singleton
class ChatController @Inject()(val messagesApi: MessagesApi)
                              (implicit actorSystem: ActorSystem, materializer: Materializer)
  extends Controller with I18nSupport {

  val chat = actorSystem.actorOf(Props[Chat], "chat")

  def socket = WebSocket.accept[String, String] { implicit request =>
    ActorFlow.actorRef(out => Props(new ClientActor(out, chat)))
  }

  def index = Action { implicit request =>
    Ok(views.html.chat())
  }
}