package com.microsoft.awt.data

import org.scalajs.nodejs.mongodb.{Collection, Db}

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Event DAO
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait EventDAO extends Collection

/**
  * Event DAO Companion
  * @author lawrence.daniels@gmail.com
  */
object EventDAO {

  /**
    * Event DAO Extensions
    * @param db the given [[Db database]]
    */
  implicit class EventDAOExtensions(val db: Db) extends AnyVal {

    @inline
    def getEventDAO(implicit ec: ExecutionContext) = {
      db.collectionFuture("events").mapTo[EventDAO]
    }

  }

}