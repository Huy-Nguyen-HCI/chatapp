package models
import java.sql.{PreparedStatement, ResultSet}

import play.api.db._

/**
  * Created by HuyNguyen on 6/5/17.
  */
class DBUtility {

}

object DBUtility {
  def getPasswordFromUsername(db: Database, username: String): String = {
    db.withConnection { conn =>
      val statement: PreparedStatement = conn.prepareStatement("SELECT password FROM users WHERE username = ?")
      statement.setString(1, username)
      val result: ResultSet = statement.executeQuery()
      if (result.next()) {
        result.getString("password")
      } else {
        null
      }
    }
  }
}
