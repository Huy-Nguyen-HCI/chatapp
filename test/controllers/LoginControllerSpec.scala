package controllers

import com.github.t3hnar.bcrypt._
import helpers.{CSRFPostTest, CachedInject}
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.db.DBApi
import play.api.db.evolutions.{Evolution, Evolutions, SimpleEvolutionsReader}
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET => GET_REQUEST, POST => POST_REQUEST, _}
import play.filters.csrf.CSRFAddToken


/**
  * Created by thangle on 8/10/17.
  */
class LoginControllerSpec extends PlaySpec with Results with GuiceOneAppPerSuite with BeforeAndAfter
    with CachedInject with CSRFPostTest {

  private var controller: LoginController = _

  private val db = getInstance[DBApi].database("default")
  private val addToken = getInstance[CSRFAddToken]

  before {
    controller = getInstance[LoginController]

    Evolutions.applyEvolutions(db)
    Evolutions.applyEvolutions(db, SimpleEvolutionsReader.forDefault(
      Evolution(
        3,
        s"INSERT INTO user VALUES (1, 'ann', '${"ann".bcrypt}', 'ann@gmail.com');"
      )
    ))
  }

  after {
    Evolutions.cleanupEvolutions(db)
  }

  "LoginController#index" must {

    "be valid" in {
      val action = addToken(controller.index())
      val result = action(FakeRequest())

      status(result) mustBe OK
    }

    "redirect to chat if user is already logged in" in {
      val action = addToken(controller.index())
      val result = action(FakeRequest().withSession(USERNAME_KEY -> "ann"))

      redirectLocation(result) mustBe Some(routes.ChatController.index().url)
    }

    "redirect to login again without connected user in session if the user is not in database" in {
      val action = addToken(controller.index())
      val result = action(FakeRequest().withSession(USERNAME_KEY -> "bob"))

      redirectLocation(result) mustBe Some(routes.LoginController.index().url)
      session(result).get(USERNAME_KEY) mustBe empty
    }
  }

  "LoginController#login" must {

    def requestWithFormData(username: String, password: String) =
      FakeRequest(POST_REQUEST, routes.LoginController.login().url)
        .withFormUrlEncodedBody("username" -> username, "password" -> password)
        .withHeaders(HOST -> "localhost")


    "let user login to chat if the information is entered correctly" in {
      val action = addToken(controller.login())
      val result = action(requestWithFormData("ann", "ann"))

      redirectLocation(result) mustBe Some(routes.ChatController.index().url)
    }

    "stay at login page if the information entered is incorrect" in {
      val action = addToken(controller.login())
      val result = action(requestWithFormData("ann", "bob"))

      status(result) mustBe OK
      flash(result).get("error") mustBe defined
    }
  }

  "LoginController#logout" must {

    "let user logout" in {
      val result = controller.logout().apply(FakeRequest())

      redirectLocation(result) mustBe Some(routes.HomeController.index().url)
      session(result).get(USERNAME_KEY) mustBe empty
    }
  }
}
