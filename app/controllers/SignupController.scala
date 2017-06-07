package controllers

import javax.inject.Inject

import play.api.mvc._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller
import play.api.data.Forms._
import models._
import dao.UserDao

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by thangle on 6/5/17.
  */
class SignupController @Inject() (val messagesApi: MessagesApi, userDao: UserDao)
                                 (implicit executionContext: ExecutionContext)
      extends Controller with I18nSupport {

  val userForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText,
      "email" -> email
    )(UserSignupData.apply)(UserSignupData.unapply)
  )

  def index = Action { implicit request =>
    Ok(views.html.signup(userForm))
  }

  def signup = Action.async { implicit request =>
    val userInfo: UserSignupData = userForm.bindFromRequest.get
    val username = userInfo.username
    val password = userInfo.password
    val email = userInfo.email

    // check if user already exists before adding new user
    userDao.findByUsername(username).flatMap {
      case Some(_) => Future.successful(Ok("username already exists"))
      case _ => {
        val newUser = User(username, password, email)
        userDao.insert(newUser).map(_ => Ok("sign up successfully"))
      }
    }
  }
}
