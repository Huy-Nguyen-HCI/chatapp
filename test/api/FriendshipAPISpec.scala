package api

import dao.FriendshipDao
import helpers.{CSRFTest, CachedInject}
import models.FriendshipStatusCode
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.db.DBApi
import play.api.db.evolutions.{Evolution, Evolutions, SimpleEvolutionsReader}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET => GET_REQUEST, POST => POST_REQUEST, _}

import scala.concurrent.ExecutionContext
import com.github.t3hnar.bcrypt._
import controllers.USERNAME_KEY


/**
  * Created by thang on 6/25/17.
  */
class FriendshipAPISpec extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfter
    with CachedInject with CSRFTest {

  private val db = getInstance[DBApi].database("default")
  private val friendshipCode = getInstance[FriendshipStatusCode]

  before {
    Evolutions.applyEvolutions(db)
    Evolutions.applyEvolutions(db, SimpleEvolutionsReader.forDefault(
      Evolution(
        3,
        s"INSERT INTO user VALUES (1, 'ann', '${"ann".bcrypt}', 'ann@gmail.com');" +
        s"INSERT INTO user VALUES (2, 'bob', '${"bob".bcrypt}', 'bob@gmail.com');" +
        "INSERT INTO user VALUES (3, 'charlie', 'charlie', 'charlie@gmail.com');" +
        "INSERT INTO user VALUES (4, 'daisy', 'daisy', 'daisy@gmail.com');" +
        s"INSERT INTO friendship VALUES (1, 3, ${friendshipCode.BLOCKED}, 3);"
      )
    ))
  }

  after {
    Evolutions.cleanupEvolutions(db)
  }

  "FriendshipAPI" must {

    val friendshipDao = getInstance[FriendshipDao]
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
      friendshipDao.getFriendship(1, 2).map { res =>
        res.get mustEqual (friendshipCode.PENDING, 1)
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
      await(friendshipDao.insertOrUpdateFriendship(1, 2, friendshipCode.PENDING))
      val apiUrl = "/api/friend/accept"
      var json = """{"sender":"bob", "receiver":"ann"}"""

      var result = apiPostRequest(apiUrl, json, "bob")
      status(result) mustBe OK

      // check value in db
      friendshipDao.getFriendship(1, 2).map { res =>
        res.get mustEqual (friendshipCode.ACCEPTED, 2)
      }

      // cannot accept if there is no pending friend request
      json = """{"sender":"daisy", "receiver":"ann"}"""
    }


    "be able to list all pending requests" in {
      await(friendshipDao.insertOrUpdateFriendship(2, 1, friendshipCode.PENDING))
      await(friendshipDao.insertOrUpdateFriendship(4, 1, friendshipCode.PENDING))

      var request = FakeRequest(GET_REQUEST, s"/api/friend/search?username=ann&status=${friendshipCode.PENDING}")
        .withHeaders("Host" -> "localhost")

      var result = route(app, request).get

      // do not authorize user that is not connected in the session
      status(result) mustBe UNAUTHORIZED

      // authorize user that is connected
      request = request.withSession(USERNAME_KEY -> "ann")
      result = route(app, request).get

      status(result) mustBe OK

      val pendings = contentAsJson(result).as[Seq[String]]
      pendings must contain only ("bob", "daisy")
    }


    "be able to remove friendship" in {
      await(friendshipDao.insertOrUpdateFriendship(2, 1, friendshipCode.PENDING))

      val json = """{"sender":"ann", "receiver":"bob"}"""
      val result = apiPostRequest("/api/friend/remove", json, "ann")
      status(result) mustBe OK

      val friendship = await(friendshipDao.getFriendship(1, 2))
      friendship.isDefined mustBe false
    }


    "be able to check friendship status" in {
      await(friendshipDao.insertOrUpdateFriendship(2, 1, friendshipCode.PENDING))

      var request = FakeRequest(GET_REQUEST, "/api/friend/check?first=ann&second=bob")
                      .withHeaders("Host" -> "localhost")
      var result = route(app, request).get

      // do not authorize the user making the request who is not connected in the session
      status(result) mustBe UNAUTHORIZED

      request = request.withSession(USERNAME_KEY -> "ann")
      result = route(app, request).get

      status(result) mustBe OK

      // check content
      val json = contentAsJson(result)
      (json \ "status").as[Int] mustBe friendshipCode.PENDING
      (json \ "actionUser").as[String] mustBe "bob"
    }
  }
}
