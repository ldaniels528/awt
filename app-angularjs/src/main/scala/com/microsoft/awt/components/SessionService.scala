package com.microsoft.awt.components

import com.microsoft.awt.models.Session
import org.scalajs.angularjs.Service
import org.scalajs.angularjs.http.Http

import scala.scalajs.js

/**
  * Session Service
  * @author lawrence.daniels@gmail.com
  */
class SessionService($http: Http) extends Service {

  /**
    * Retrieves a user session by ID or creates a new anonymous session
    * @param id the given user session ID
    * @return a promise of a [[com.microsoft.awt.models.Session user session]]
    */
  def getSession(id: String) = $http.get[Session](s"/api/session/$id")

  /**
    * Attempts to retrieve all of the sessions for the given collection of user IDs
    * @param userIDs the given collection of user IDs
    * @return a promise of an array of user sessions
    */
  def getSessions(userIDs: js.Array[String]) = {
    $http.get[js.Array[Session]](s"/api/sessions?${userIDs.map(id => s"userIDs=$id").mkString("&")}")
  }

}
