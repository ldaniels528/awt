package com.microsoft.awt.data

import org.scalajs.nodejs.mongodb.{Db, _}

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Session DAO
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait SessionDAO extends Collection

/**
  * Session DAO Companion
  * @author lawrence.daniels@gmail.com
  */
object SessionDAO {

  /**
    * Session DAO Enrichment
    * @param sessionDAO the given [[SessionDAO Session DAO]]
    */
  implicit class SessionDAOEnrichment(val sessionDAO: SessionDAO) extends AnyVal {

    def findAndUpdateByID(sessionID: String, isAnonymous: Boolean = false)(implicit mongodb: MongoDB) =  {
      sessionDAO.findOneAndUpdate(
        filter = "_id" $eq sessionID.$oid,
        update = $set(doc("isAnonymous" -> isAnonymous, "lastUpdated" -> js.Date.now())))
    }

  }

  /**
    * Session DAO Extensions
    * @param db the given [[Db DB]]
    */
  implicit class SessionDAOExtensions(val db: Db) extends AnyVal {

    @inline
    def getSessionDAO(implicit ec: ExecutionContext) = {
      db.collectionFuture("sessions").mapTo[SessionDAO]
    }

  }

}