package controllers

import javax.inject.{Singleton, Inject}

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}

import dao.UserDao

import scala.concurrent.{ExecutionContext, Future}

import com.github.t3hnar.bcrypt._


/**
  * Handles logging in and creating accounts
  */
@Singleton
class LoginController @Inject() (val messagesApi: MessagesApi, userDao: UserDao)
                                (implicit executionContext: ExecutionContext)
      extends Controller with I18nSupport {

  val userForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(UserLoginData.apply)(UserLoginData.unapply)
  )

  def index = Action.async { implicit request =>
    val user = request.session.get(USERNAME_KEY)

    if (user.isDefined) {
      // check if user exists in database
      val query = userDao.findByUsername(user.get)

      query map {
        case Some(_) => Redirect(routes.ChatController.index())
        case None => Redirect(routes.LoginController.index()).withSession(request.session - USERNAME_KEY)
      }
    } else {
      Future.successful(Ok(views.html.login(userForm)))
    }
  }

  def login = Action.async { implicit request =>
    userForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.login(errorForm)).flashing("error" -> "some required fields are empty"))
      },

      userLogin => {
        val username : String = userLogin.username
        val password : String = userLogin.password

        // check if login password matches user password
        val query = userDao.findByUsername(username)
        query map {
          case Some(user) if password.isBcrypted(user.password) =>
            Redirect(routes.ChatController.index()).withSession(USERNAME_KEY -> username)

          case _ =>
            val errorMsg = "incorrect username or password"
            val formWithErrors = userForm.withGlobalError(errorMsg)
            Ok(views.html.login(formWithErrors)).flashing("error" -> errorMsg)
        }
      }
    )
  }

  def logout = Action { implicit request =>
    Redirect(routes.HomeController.index()).withNewSession
  }
}

// user login form
case class UserLoginData(username: String, password: String)
