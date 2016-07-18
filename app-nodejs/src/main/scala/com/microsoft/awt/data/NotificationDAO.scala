package com.microsoft.awt.data

import org.scalajs.nodejs.mongodb.{Collection, Db}

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Notification DAO
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait NotificationDAO extends Collection

/**
  * Notification DAO Companion
  * @author lawrence.daniels@gmail.com
  */
object NotificationDAO {

  /**
    * Notification DAO Extensions
    * @param db the given [[Db database]]
    */
  implicit class NotificationDAOExtensions(val db: Db) extends AnyVal {

    @inline
    def getNotificationDAO(implicit ec: ExecutionContext) = {
      db.collectionFuture("notifications").mapTo[NotificationDAO]
    }

  }

}