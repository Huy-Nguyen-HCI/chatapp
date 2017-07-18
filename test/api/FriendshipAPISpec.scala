package api

import dao.FriendshipDao
import helpers.{CSRFTest, CachedInject}
import models.Friendship

import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.Application
import play.api.db.DBApi
import play.api.db.evolutions.{Evolution, Evolutions, SimpleEvolutionsReader}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{POST => POST_REQUEST, _}

import scala.concurrent.ExecutionContext
import com.github.t3hnar.bcrypt._
import controllers.USERNAME_KEY


/**
  * Created by thang on 6/25/17.
  */
class FriendshipAPISpec extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfter
    with CachedInject with CSRFTest {

  private val db = getInstance[DBApi].database("default")

  before {
    Evolutions.applyEvolutions(db)
    Evolutions.applyEvolutions(db, SimpleEvolutionsReader.forDefault(
      Evolution(
        3,
        s"INSERT INTO user VALUES (1, 'ann', '${"ann".bcrypt}', 'ann@gmail.com');" +
        s"INSERT INTO user VALUES (2, 'bob', '${"bob".bcrypt}', 'bob@gmail.com');" +
        "INSERT INTO user VALUES (3, 'charlie', 'charlie', 'charlie@gmail.com');" +
        "INSERT INTO user VALUES (4, 'daisy', 'daisy', 'daisy@gmail.com');" +
        s"INSERT INTO friendship VALUES (1, 3, ${Friendship.STATUS_BLOCKED}, 3);"
      )
    ))
  }

  after {
    Evolutions.cleanupEvolutions(db)
  }

  "FriendshipAPI" must {

    val friendshipDAO = getInstance[FriendshipDao]
    implicit val executionContext = getInstance[ExecutionContext]

    def apiPostRequest(url: String, json: String, username: String = "")
                      (implicit app: Application) = {

      val request = addToken(FakeRequest(
          method = POST_REQUEST,
          path = url
        )
        .withJsonBody(Json.parse(json))
        .withHeaders(HOST -> "localhost")
        .withSession(USERNAME_KEY -> username)
      )

      route(app, request).get
    }


    "be able to process add friend request" in {
      val apiUrl = "/api/friend/add"
      var json = """{"sender":"ann", "receiver":"bob"}"""

      // check status of POST request
      var result = apiPostRequest(apiUrl, json, "ann")
      status(result) mustEqual OK

      // check value in db
      friendshipDAO.getFriendship(1, 2).map { res =>
        res.get mustEqual (Friendship.STATUS_PENDING, 1)
      }

      // cannot request friend when blocked
      json = """{"sender":"ann", "receiver":"charlie"}"""

      result = apiPostRequest(apiUrl, json, "ann")
      status(result) mustEqual BAD_REQUEST

      // not process request that is not authenticated
      json = """{"sender":"bob", "receiver":"ann"}"""

      result = apiPostRequest(apiUrl, json, "random_guy")
      status(result) mustBe UNAUTHORIZED
    }


    "be able to accept friend request" in {
      await(friendshipDAO.insertOrUpdateFriendship(1, 2, Friendship.STATUS_PENDING))
      val apiUrl = "/api/friend/accept"
      var json = """{"sender":"bob", "receiver":"ann"}"""

      var result = apiPostRequest(apiUrl, json, "bob")
      status(result) mustBe OK

      // check value in db
      friendshipDAO.getFriendship(1, 2).map { res =>
        res.get mustEqual (Friendship.STATUS_ACCEPTED, 2)
      }

      // cannot accept if there is no pending friend request
      json = """{"sender":"daisy", "receiver":"ann"}"""
    }
  }
}
