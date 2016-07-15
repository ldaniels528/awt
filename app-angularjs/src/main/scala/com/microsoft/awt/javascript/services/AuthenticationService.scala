package com.microsoft.awt.javascript.services

import com.microsoft.awt.javascript.forms.LoginForm
import com.microsoft.awt.javascript.models.{AuthToken, Session}
import com.microsoft.awt.javascript.services.AuthenticationService.LogoutResponse
import org.scalajs.angularjs.Service
import org.scalajs.angularjs.http.Http

import scala.scalajs.js

/**
  * Authentication Service
  * @author lawrence.daniels@gmail.com
  */
class AuthenticationService($http: Http) extends Service {

  /**
    * Attempts to retrieve an authentication token from the server
    * @param username the username for whom the token is being requested
    * @return a new time-sensitive [[AuthToken authentication token]]
    */
  def getAuthToken(username: String) = $http.get[AuthToken](s"/api/auth/$username/token")

  /**
    * Attempts to authenticate a user
    * @param tokenId   the given token ID
    * @param loginForm the given [[LoginForm login credentials]]
    * @return a promise of an authenticated [[Session session]]
    */
  def login(tokenId: String, loginForm: LoginForm) = $http.post[Session](s"/api/auth/$tokenId/login", loginForm)

  /**
    * Signs the user belong to the token ID out of the system
    * @param tokenId the given token ID
    * @return the promise of a successful outcome
    */
  def logout(tokenId: String) = $http.delete[LogoutResponse](s"/api/auth/$tokenId/token")

}

/**
  * Authentication Service Companion
  * @author lawrence.daniels@gmail.com
  */
object AuthenticationService {

  /**
    * Represents a logout response
    */
  @js.native
  trait LogoutResponse extends js.Object {
    /** Is 1 if the command executed correctly. */
    var ok: Int = js.native

    /** The total count of documents deleted. */
    var n: Int = js.native
  }

  /**
    * Logout Response Companion
    */
  object LogoutResponse {

    /**
      * Logout Result Enrichment
      * @param response the given [[LogoutResponse response]]
      */
    implicit class LogoutResultEnrichment(val response: LogoutResponse) extends AnyVal {

        def isOk = response.ok == 1 && response.n == 1
    }
  }

}
