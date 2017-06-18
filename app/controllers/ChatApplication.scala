package controllers

import javax.inject.Inject

import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.api.libs.json.JsValue
import play.api.libs.iteratee.{Concurrent, Enumeratee}
import play.api.libs.EventSource
import play.api.libs.concurrent.Execution.Implicits._



class ChatApplication @Inject() (val messagesApi: MessagesApi) extends Controller with I18nSupport {

  val chatForm = Form(
    mapping(
      "message" -> text
    )(ChatData.apply)(ChatData.unapply)
  )

  /** Central hub for distributing chat messages */
  val (chatOut, chatChannel) = Concurrent.broadcast[JsValue]

  /** Controller action serving AngularJS chat page */
  def index = Action { implicit request => Ok(views.html.chat2(chatForm)) }

  /** Controller action for POSTing chat messages */
  def postMessage = Action(parse.json) { req => chatChannel.push(req.body); Ok }

  /** Enumeratee for filtering messages based on room */
  def filter(room: String) = Enumeratee.filter[JsValue] { json: JsValue => (json \ "room").as[String] == room }

  /** Enumeratee for detecting disconnect of SSE stream */
  def connDeathWatch(addr: String): Enumeratee[JsValue, JsValue] =
    Enumeratee.onIterateeDone{ () => println(addr + " - SSE disconnected") }

  /** Controller action serving activity based on room */
  def chatFeed(room: String) = Action { req =>
    println(req.remoteAddress + " - SSE connected")
    Ok.feed(chatOut
      &> filter(room)
      &> Concurrent.buffer(50)
      &> connDeathWatch(req.remoteAddress)
      &> EventSource()
    ).as("text/event-stream")
  }

  def dummy = Action {
    Ok("hello")
  }
}

case class ChatData(message: String)