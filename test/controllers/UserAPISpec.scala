package controllers

import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.db.evolutions.Evolutions

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration
import dao.UserDao
import play.api.db.DBApi
import helpers.CachedInject
import play.api.libs.json.JsUndefined
import play.api.test.{FakeRequest, Writeables}
import play.api.test.Helpers.{GET => GET_REQUEST, _}


/**
  * Created by thang on 6/20/17.
  */
class UserAPISpec extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfter with CachedInject
                               with Writeables {

  import models._

  // Database instance
  private val db = getInstance[DBApi].database("default")

  // execution context for executing asynchronous task
  private implicit val executionContext = getInstance[ExecutionContext]


  before {
    Evolutions.applyEvolutions(db)
  }

  after {
    Evolutions.cleanupEvolutions(db)
  }

  "API" should {

    val userDao = getInstance[UserDao]

    "give user by user name" in {
      Await.result(userDao.insert(User(None, "test", "test", "test@gmail.com")), Duration.Inf)

      val request = FakeRequest(GET_REQUEST, "/api/user/test").withHeaders("Host" -> "localhost")
      val result = route(app, request).get

      status(result) mustEqual OK
      contentType(result) mustBe Some("application/json")

      // test value
      val jsonContent = contentAsJson(result)
      (jsonContent \ "username").as[String] mustEqual "test"
      (jsonContent \ "password") mustBe a [JsUndefined]
      (jsonContent \ "email").as[String] mustEqual "test@gmail.com"
    }
  }
}
