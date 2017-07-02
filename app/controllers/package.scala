import play.api.mvc._

/**
  * Created by thang on 6/28/17.
  */
package object controllers {

  val USERNAME_KEY = "connected"

  /*
   * Define secured action that authenticates user session
   */
  trait Secured  {
    def username(request: RequestHeader): Option[String] = request.session.get(USERNAME_KEY)
    def onUnauthorized(request: RequestHeader) = Results.Unauthorized
    def isAuthenticated(f: => String => Request[AnyContent] => Result): EssentialAction = {
      Security.Authenticated(username, onUnauthorized) { user =>
        Action(request => f(user)(request))
      }
    }
  }
}
