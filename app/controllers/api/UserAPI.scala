package controllers.api

import javax.inject.Inject

import dao.UserDao
import models.User

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
  * Created by thang on 6/19/17.
  */
class UserAPI @Inject() (userDao: UserDao)(implicit executionContext: ExecutionContext)
              extends Controller {

  private val ignore = OWrites[Any](_ => Json.obj())

  implicit val userWrites: Writes[User] = (
    (JsPath \ "id").writeNullable[Long] and
    (JsPath \ "username").write[String] and
    ignore and
    (JsPath \ "email").write[String]
  )(unlift(User.unapply))


  def findByUsername(name: String) = Action.async {
    userDao.findByUsername(name).map {
      case Some(user) => Ok(Json.toJson(user))
      case None => NotFound
    }
  }
}
