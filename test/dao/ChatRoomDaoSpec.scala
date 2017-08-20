package dao

import java.sql.SQLException

import helpers.CachedInject
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.db.DBApi
import play.api.db.evolutions.{Evolution, Evolutions, SimpleEvolutionsReader}
import play.api.test.Helpers._


/**
  * Created by thang on 6/22/17.
  */
class ChatRoomDaoSpec extends PlaySpec with GuiceOneAppPerSuite with CachedInject
  with BeforeAndAfter {

  private val db = getInstance[DBApi].database("default")

  before {
    Evolutions.applyEvolutions(db)
    Evolutions.applyEvolutions(db, SimpleEvolutionsReader.forDefault(
      Evolution(
        4,
        "INSERT INTO user VALUES (1, 'ann', 'ann', 'ann@gmail.com');" +
        "INSERT INTO user VALUES (2, 'bob', 'bob', 'bob@gmail.com');" +
        "INSERT INTO user VALUES (3, 'charlie', 'charlie', 'charlie@gmail.com');"
      )
    ))
  }

  after {
    Evolutions.cleanupEvolutions(db)
  }

  private val chatRoomDao = getInstance[ChatRoomDao]

  "ChatRoomDao" should {

    "be able to insert a room with an owner id" in {
      await(chatRoomDao.insert(1)) mustEqual 1
      await(chatRoomDao.insert(2)) mustEqual 2
    }

    "be able to get the id of the owner of any room" in {
      await(chatRoomDao.insert(1))
      await(chatRoomDao.getOwnerId(1)) mustBe Some(1)
    }

    "be able to handle foreign keys correctly" in {
      try {
        await(chatRoomDao.insert(4))
        fail()
      } catch {
        case _: SQLException =>
      }
    }

    "be able to add new participant to a given room" in {
      val roomId = await(chatRoomDao.insert(1))
      await(chatRoomDao.addParticipant(roomId, 2))
      val res = await(chatRoomDao.getAllParticipantIds(roomId))
      res must contain only(1, 2)
    }
  }
}
