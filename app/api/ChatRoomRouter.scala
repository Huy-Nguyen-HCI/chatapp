package api

import play.api.routing.SimpleRouter
import play.api.routing.Router.Routes
import play.api.routing.sird._

import javax.inject.Inject

/**
  * Created by thangle on 8/20/17.
  */
class ChatRoomRouter @Inject()(controller: ChatRoomAPI) extends SimpleRouter {
  val prefix = "/api/chatroom"

  override def routes: Routes = {
    case GET(p"/participants" ? q"room=${long(roomId)}") =>
      controller.listParticipants(roomId)

    case POST(p"/add" ? q"room=${long(roomId)}" & q"username=$username") =>
      controller.addParticipant(roomId, username)

    case GET(p"/list") =>
      controller.listAccessibleRoomIds

    case POST(p"/create") =>
      controller.createRoom
  }
}
