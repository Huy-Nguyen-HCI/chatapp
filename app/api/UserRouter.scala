package api

import javax.inject.Inject

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

/**
  * Created by thangle on 7/18/17.
  */
class UserRouter @Inject()(controller: UserAPI) extends SimpleRouter {

  val prefix = "/api/users"

  override def routes: Routes = {
    case GET(p"/") =>
      controller.list

    case GET(p"/$username") =>
      controller.findByUsername(username)
  }
}
