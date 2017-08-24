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
    case GET(p"/${long(roomId)}/participants") =>
      controller.listParticipants(roomId)

    case POST(p"/create") =>
      controller.createRoom
  }
}
