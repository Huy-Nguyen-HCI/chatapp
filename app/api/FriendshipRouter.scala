package api

import javax.inject.Inject

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._


/**
  * Created by thangle on 7/18/17.
  */
class FriendshipRouter @Inject()(controller: FriendshipAPI) extends SimpleRouter {
  val prefix = "/api/friend"

  override def routes: Routes = {
    case POST(p"/add") =>
      controller.sendFriendRequest

    case POST(p"/accept") =>
      controller.acceptFriendRequest

    case POST(p"/remove") =>
      controller.removeFriendship()

    case GET(p"/search" ? q"username=$username" & q"status=${int(status)}") =>
      controller.listUsersMakingStatus(username, status)

    case GET(p"/check" ? q"first=$firstUser" & q"second=$secondUser") =>
      controller.getStatus(firstUser, secondUser)
  }
}
