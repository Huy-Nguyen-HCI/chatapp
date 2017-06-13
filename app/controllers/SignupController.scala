package controllers

import java.sql.SQLException
import javax.inject.Inject

import play.api.mvc._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller
import play.api.data.Forms._
import models.User
import dao.UserDao

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import com.github.t3hnar.bcrypt._

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
      "retypePassword" -> nonEmptyText,
      "email" -> email
    )(UserSignupData.apply)(UserSignupData.unapply)
  )

  def index = Action { implicit request =>
    Ok(views.html.signup(userForm))
  }

  def signup = Action.async { implicit request =>
    userForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.signup(errorForm)))
      },

      userInfo => {
        val username = userInfo.username
        val password = userInfo.password
        val email = userInfo.email

        for {
          dbUsername <- userDao.findByUsername(username)
          dbEmail <- userDao.findByEmail(email)
          res <-
            if (dbUsername.isEmpty && dbEmail.isEmpty) {
              Redirect("/login")
            } else {
              Redirect("/")
            }
        } yield res
      }
    )
  }
}

// user sign up form
case class UserSignupData(username: String, password: String,
                          retypePassword: String, email: String)