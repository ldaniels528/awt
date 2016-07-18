package com.microsoft.awt.javascript.components

import com.microsoft.awt.javascript.models.InboxMessage
import org.scalajs.angularjs.Service
import org.scalajs.angularjs.http.Http

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Inbox Message Service
  * @author lawrence.daniels@gmail.com
  */
class InboxMessageService($http: Http) extends Service {

  /**
    * Retrieves messages for the given user
    * @param userID the ID of the user that owns the messages
    * @return a promise of an array of [[InboxMessage messages]]
    */
  def getMessages(userID: String)(implicit ec: ExecutionContext) = {
    $http.get[js.Array[InboxMessage]](s"/api/messages/user/$userID")
  }

}

