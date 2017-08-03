package dao

import helpers.CachedInject
import models.FriendshipStatusCode
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.db.DBApi
import play.api.db.evolutions.{Evolution, Evolutions, SimpleEvolutionsReader}
import play.api.test.Helpers._


/**
  * Created by thang on 6/22/17.
  */
class FriendshipDAOSpec extends PlaySpec with GuiceOneAppPerSuite with CachedInject
    with BeforeAndAfter {

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

  "FriendshipDAO" must {
    val friendshipDao = getInstance[FriendshipDao]
    val friendshipCode = getInstance[FriendshipStatusCode]

    "be able to insert friendship" in {

      // the ids are in correct order (first < second)
      await(friendshipDao.insertOrUpdateFriendship(1, 2, friendshipCode.PENDING))

      var res = await(friendshipDao.getFriendship(1, 2)).get
      res._1 mustEqual friendshipCode.PENDING
      res._2 mustEqual 1

      // the ids are not in correct order (first > second)
      await(friendshipDao.insertOrUpdateFriendship(3, 1, friendshipCode.PENDING))

      res = await(friendshipDao.getFriendship(1, 3)).get
      res._1 mustEqual friendshipCode.PENDING
      res._2 mustEqual 3
    }

    "be able to update friendship" in {

      await(friendshipDao.insertOrUpdateFriendship(1, 2, friendshipCode.PENDING))
      await(friendshipDao.insertOrUpdateFriendship(2, 1, friendshipCode.ACCEPTED))

      val res = await(friendshipDao.getFriendship(1, 2)).get
      res._1 mustEqual friendshipCode.ACCEPTED
      res._2 mustEqual 2
    }

    "be able to get pending friend requests" in {
      await(friendshipDao.insertOrUpdateFriendship(2, 1, friendshipCode.PENDING))
      await(friendshipDao.insertOrUpdateFriendship(3, 1, friendshipCode.PENDING))

      val res = await(friendshipDao.listUsersMakingStatus(1, friendshipCode.PENDING))

      res must contain only (2, 3)
    }

  }
}
