package helpers

import play.api.Application
import play.api.test.FakeRequest
import play.filters.csrf.{CSRFConfigProvider, CSRFFilter}

/**
  * Created by thang on 7/1/17.
  */
trait CSRFPostTest {
  def addPostToken[T](fakeRequest: FakeRequest[T])(implicit app: Application): FakeRequest[T] = {
    val csrfConfig     = app.injector.instanceOf[CSRFConfigProvider].get
    val csrfFilter     = app.injector.instanceOf[CSRFFilter]
    val token          = csrfFilter.tokenProvider.generateToken

    fakeRequest
      .withHeaders(csrfConfig.headerName -> token)
      .withSession(csrfConfig.tokenName -> token)
  }
}