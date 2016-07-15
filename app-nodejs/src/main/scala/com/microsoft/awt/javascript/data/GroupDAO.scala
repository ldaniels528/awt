package com.microsoft.awt.javascript.data

import org.scalajs.nodejs.mongodb.{Collection, CollectionOptions, Db}

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Group DAO
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait GroupDAO extends Collection

/**
  * Group DAO Companion
  * @author lawrence.daniels@gmail.com
  */
object GroupDAO {

  /**
    * Group DAO Extensions
    * @param db the given [[Db database]]
    */
  implicit class GroupDAOExtensions(val db: Db) extends AnyVal {

    @inline
    def getGroupDAO(implicit ec: ExecutionContext) = {
      db.collectionFuture("groups", new CollectionOptions()).mapTo[GroupDAO]
    }

  }

}