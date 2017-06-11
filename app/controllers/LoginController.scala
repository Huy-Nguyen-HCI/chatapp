package controllers

import javax.inject.Inject

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}

import dao.UserDao

import scala.concurrent.{ExecutionContext, Future}


/**
  * Handles logging in and creating accounts
  */
class LoginController @Inject() (val messagesApi: MessagesApi, userDao: UserDao)
                                (implicit executionContext: ExecutionContext)
      extends Controller with I18nSupport {

  val userForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(UserLoginData.apply)(UserLoginData.unapply)
  )

  def index = Action { implicit request =>
    Ok(views.html.login(userForm))
  }

  def login = Action.async { implicit request =>
    userForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.login(errorForm)))
      },

      userLogin => {
        val username : String = userLogin.username
        val password : String = userLogin.password

        // check if login password matches user password
        val query = userDao.findByUsername(username)
        query map {
          case Some(user) if user.password == password =>
            Redirect("/").withSession("connected" -> username)

          case _ => BadRequest("incorrect username and password")
        }
      }
    )

  }

  def logout = Action { implicit request =>
    Redirect("/").withNewSession
  }
}

// user login form
case class UserLoginData(username: String, password: String)
