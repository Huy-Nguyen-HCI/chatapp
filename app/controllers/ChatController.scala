package controllers
import javax.inject.{Inject, Singleton}

import dao.UserDao
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by thangle on 8/10/17.
  */
@Singleton
class ChatController @Inject() (val messagesApi: MessagesApi)
                               (implicit val userDao: UserDao, executionContext: ExecutionContext)
      extends Controller with I18nSupport {

  def index = UserAuthenticated {
    Action.async { implicit request =>
      Future.successful(Ok(views.html.chat()))
    }
  }
}
