package controllers

import javax.inject.Inject

import play.api.mvc._
import play.api.data._
import play.api.data.validation.Constraints.nonEmpty
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller
import play.api.data.Forms._

import models.User
import dao.UserDao

import scala.concurrent.{ExecutionContext, Future}
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
      "email" -> email.verifying(nonEmpty)
    )(UserSignupData.apply)(UserSignupData.unapply)
  )

  def index = Action { implicit request =>
    request.session.get(USERNAME_KEY) match {
      case None => Ok(views.html.signup(userForm))
      case Some(_) => Redirect(routes.ChatController.index())
    }
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

        val dbUsername = userDao.findByUsername(username)
        val dbEmail = userDao.findByEmail(email)

        val isExist = for { v1 <- dbUsername; v2 <- dbEmail } yield (v1, v2)

        // check whether the username or email already exists
        isExist flatMap {
          case (Some(_), _) =>
            val errorForm = createErrorForm(userInfo, "username", "this username already exists")
            Future.successful(Ok(views.html.signup(errorForm)))
          case (_, Some(_)) =>
            val errorForm = createErrorForm(userInfo, "email", "this email already exists")
            Future.successful(Ok(views.html.signup(errorForm)))
          case (None, None) =>
            val newUser = User(None, username, password.bcrypt, email)
            userDao.insert(newUser).map(_ => Redirect("/").withSession(USERNAME_KEY -> username))
        }

      }
    )
  }

  private def createErrorForm(info: UserSignupData, errorField: String, message: String) = {
    val errorForm = userForm.withError(errorField, message)
    val formFields = UserSignupData(info.username, "", "", info.email) // hide passwords
    errorForm.fill(formFields)
  }

}

// user sign up form
case class UserSignupData(username: String, password: String,
                          retypePassword: String, email: String)