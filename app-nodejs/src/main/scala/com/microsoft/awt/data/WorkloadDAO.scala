package com.microsoft.awt.data

import org.scalajs.nodejs.mongodb.{Collection, CollectionOptions, Db}

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Workload DAO
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait WorkloadDAO extends Collection

/**
  * Workload DAO Companion
  * @author lawrence.daniels@gmail.com
  */
object WorkloadDAO {

  /**
    * Workload DAO Extensions
    * @param db the given [[Db database]]
    */
  implicit class WorkloadDAOExtensions(val db: Db) extends AnyVal {

    @inline
    def getWorkloadDAO(implicit ec: ExecutionContext) = {
      db.collectionFuture("workloads", new CollectionOptions()).mapTo[WorkloadDAO]
    }

  }

}
