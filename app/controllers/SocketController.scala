package controllers

import javax.inject.{Inject, Singleton}

import akka.actor.{ActorSystem, Props}
import play.api.mvc._
import actors._
import akka.stream.Materializer
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.streams.ActorFlow

import scala.concurrent.Future

@Singleton
class SocketController @Inject()(val messagesApi: MessagesApi)
                                (implicit actorSystem: ActorSystem, materializer: Materializer)
  extends Controller with I18nSupport {

  private val central = actorSystem.actorOf(Props[CentralActor], "central")

  def socket = WebSocket.acceptOrResult[String, String] { implicit request =>
    Future.successful(request.session.get(USERNAME_KEY) match {
      case None => Left(Forbidden)
      case Some(name) => Right(ActorFlow.actorRef { out =>
        Props(new ClientActor(out, central, name))
      })
    })
  }
}