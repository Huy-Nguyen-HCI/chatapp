import dao.UserDao
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by thang on 6/28/17.
  */
package object controllers {

  val USERNAME_KEY = "connected"

  case class UserAuthenticated[A](action: Action[A])(implicit userDao: UserDao, ec: ExecutionContext)
    extends Action[A] with Results {

    def apply(request: Request[A]): Future[Result] = {
      request.session.get(USERNAME_KEY) match {
        case Some(username) =>
          // check if the user actually exist in the database (in case the user is removed in the database but
          // the browser still stores the session)
          userDao.findByUsername(username).flatMap {
            case Some(_) => action(request)
            case None =>
              Future.successful(Redirect(routes.LoginController.index()).withSession(request.session - USERNAME_KEY))
          }
        case None => Future.successful(Redirect(routes.LoginController.index()))
      }
    }

    lazy val parser: BodyParser[A] = action.parser
  }
}
