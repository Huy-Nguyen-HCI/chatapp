import play.api.mvc._

import scala.concurrent.Future
import controllers.USERNAME_KEY


package object api {
  case class SessionAuthenticated[A](username: String)(action: Action[A]) extends Action[A] with Results {

    def apply(request: Request[A]): Future[Result] = {
      val connectedUser = request.session.get(USERNAME_KEY)

      if (connectedUser.isDefined && username == connectedUser.get)
        action(request)
      else
        Future.successful(Unauthorized(views.html.defaultpages.unauthorized()))
    }

    lazy val parser: BodyParser[A] = action.parser
  }
}
