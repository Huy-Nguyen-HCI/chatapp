package controllers
import javax.inject.{Inject, Singleton}

import dao.UserDao
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
  * Created by thangle on 8/14/17.
  */
@Singleton
class UserDetailController @Inject()(userDao: UserDao)(implicit ec: ExecutionContext) extends Controller {

  def index(username: String) = Action.async { implicit request =>
    userDao.findByUsername(username) map {
      case Some(user) => Ok(views.html.userDetail(user))
      case None => BadRequest("This user does not exist")
    }
  }
}
