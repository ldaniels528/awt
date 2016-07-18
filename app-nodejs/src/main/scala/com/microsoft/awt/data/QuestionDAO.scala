package com.microsoft.awt.data

import org.scalajs.nodejs.mongodb.{Collection, Db}

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Question DAO
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait QuestionDAO extends Collection

/**
  * Question DAO Companion
  * @author lawrence.daniels@gmail.com
  */
object QuestionDAO {

  /**
    * Question DAO Extensions
    * @param db the given [[Db database]]
    */
  implicit class QuestionDAOExtensions(val db: Db) extends AnyVal {

    @inline
    def getQuestionDAO(implicit ec: ExecutionContext) = {
      db.collectionFuture("questions").mapTo[QuestionDAO]
    }

  }

}