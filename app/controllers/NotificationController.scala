package controllers

import javax.inject.{Inject, Singleton}

import actors.{Notification, NotificationClientActor}
import akka.actor.{ActorSystem, Props}
import akka.stream.Materializer
import play.api.libs.streams.ActorFlow
import play.api.mvc._

import scala.concurrent.Future

/**
  * Created by thangle on 7/25/17.
  */
@Singleton
class NotificationController @Inject()(implicit actorSystem: ActorSystem, materializer: Materializer)
  extends Controller {

  private val notification = actorSystem.actorOf(Props[Notification], "notification")

  def socket = WebSocket.acceptOrResult[String, String] { implicit request =>
    Future.successful(request.session.get(USERNAME_KEY) match {
      case None => Left(Forbidden)
      case Some(name) => Right(ActorFlow.actorRef { out =>
        Props(new NotificationClientActor(out, notification, name))
      })
    })
  }
}