package controllers

import dao.FriendshipDao
import helpers.CachedInject
import models.Friendship
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.db.DBApi
import play.api.db.evolutions.{Evolution, Evolutions, SimpleEvolutionsReader}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{POST => POST_REQUEST, _}

import scala.concurrent.ExecutionContext

/**
  * Created by thang on 6/25/17.
  */
class FriendshipAPISpec extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfter
    with CachedInject {

  private val db = getInstance[DBApi].database("default")

  before {
    Evolutions.applyEvolutions(db)
    Evolutions.applyEvolutions(db, SimpleEvolutionsReader.forDefault(
      Evolution(
        3,
        "INSERT INTO user VALUES (1, 'ann', 'ann', 'ann@gmail.com');" +
        "INSERT INTO user VALUES (2, 'bob', 'bob', 'bob@gmail.com');" +
        "INSERT INTO user VALUES (3, 'charlie', 'charlie', 'charlie@gmail.com');"
      )
    ))
  }

  after {
    Evolutions.cleanupEvolutions(db)
  }

  "FriendshipAPI" must {

    val friendshipDAO = getInstance[FriendshipDao]
    implicit val executionContext = getInstance[ExecutionContext]

    "be able to process add friend request" in {
      val json = """{"sender":"ann", "receiver":"bob"}"""

      val request = FakeRequest(
        method = POST_REQUEST,
        path = controllers.api.routes.FriendshipAPI.sendFriendRequest().url
      ).withJsonBody(Json.parse(json)).withHeaders(HOST -> "localhost")

      // check status of POST request
      val result = route(app, request).get
      status(result) mustEqual OK

      // check value in db
      friendshipDAO.checkFriendship(1, 2).map { res =>
        val status = res.get._1
        val actionId = res.get._2
        status mustEqual Friendship.STATUS_PENDING
        actionId mustEqual 1
      }
    }
  }
}
