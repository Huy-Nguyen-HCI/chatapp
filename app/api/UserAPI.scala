package api

import javax.inject.Inject

import dao.UserDao
import models.User
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
  * Created by thang on 6/19/17.
  */
class UserAPI @Inject() (userDao: UserDao)(implicit executionContext: ExecutionContext)
              extends Controller {

  private val ignore = OWrites[Any](_ => Json.obj())

  // only display necessary information
  implicit val userWrites: Writes[User] = (
    ignore and
    (JsPath \ "username").write[String] and
    ignore and
    ignore
  )(unlift(User.unapply))


  def findByUsername(name: String) = Action.async {
    userDao.findByUsername(name).map {
      case Some(user) => Ok(Json.toJson(user))
      case None => NotFound
    }
  }

  /*
   * Get the list of all usernames
   */
  def list = Action.async {
    userDao.list.map { res => Ok(Json.toJson(res.map(user => user.username))) }
  }
}
