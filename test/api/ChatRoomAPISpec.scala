package api

import dao.ChatRoomDao
import controllers.USERNAME_KEY
import helpers.{CSRFPostTest, CachedInject, TestConstants}
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.db.DBApi
import play.api.db.evolutions.{Evolution, Evolutions, SimpleEvolutionsReader}
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET => GET_REQUEST, POST => POST_REQUEST, _}


/**
  * Created by thang on 6/25/17.
  */
class ChatRoomAPISpec extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfter
  with CachedInject with CSRFPostTest {

  private val db = getInstance[DBApi].database("default")
  private val chatRoomDao = getInstance[ChatRoomDao]

  before {
    Evolutions.applyEvolutions(db)
    Evolutions.applyEvolutions(db, SimpleEvolutionsReader.forDefault(
      Evolution(
        TestConstants.NEXT_EVOLUTION_SCRIPT_NUM,
        s"INSERT INTO user VALUES (1, 'ann', 'ann', 'ann@gmail.com');" +
        s"INSERT INTO user VALUES (2, 'bob', 'bob', 'bob@gmail.com');" +
        s"INSERT INTO user VALUES (3, 'charlie', 'charlie', 'charlie@gmail.com');"
      )
    ))
  }

  after {
    Evolutions.cleanupEvolutions(db)
  }

  "ChatRoomAPI#listParticipants" should {

    "be able to get the list of all participants in a given room" in {
      val roomId = await(chatRoomDao.insertRoom(1))
      await(chatRoomDao.addParticipant(roomId, 2))

      val request = FakeRequest(GET_REQUEST, s"/api/chatroom/$roomId/participants")
        .withHeaders(HOST -> "localhost")
        .withSession(USERNAME_KEY -> "ann")

      val result = route(app, request).get

      status(result) mustBe OK
      contentAsJson(result).as[Seq[String]] must contain only ("ann", "bob")
    }

    "not allow a user who is not in the room to query the participant list" in {
      val roomId = await(chatRoomDao.insertRoom(1))
      await(chatRoomDao.addParticipant(roomId, 2))

      val request = FakeRequest(GET_REQUEST, s"/api/chatroom/$roomId/participants")
        .withHeaders(HOST -> "localhost")
        .withSession(USERNAME_KEY -> "charlie")

      val result = route(app, request).get

      status(result) mustBe FORBIDDEN
    }
  }

  "ChatRoomAPI#insertRoom" should {

    "let the logged in user insert a new room" in {
      var request = FakeRequest(POST_REQUEST, "/api/chatroom/create")
        .withHeaders(HOST -> "localhost")
        .withSession(USERNAME_KEY -> "ann")

      request = addPostToken(request)

      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "roomId").asOpt[Long] mustBe defined

      // check in database
      val roomId = (contentAsJson(result) \ "roomId").as[Long]
      await(chatRoomDao.getOwnerId(roomId)) mustEqual 1
      await(chatRoomDao.getAllParticipantIds(roomId)) must contain only 1
    }
  }
}

