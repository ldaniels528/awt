package com.microsoft.awt.data

import com.microsoft.awt.models.User
import org.scalajs.nodejs.mongodb._
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * User DAO
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait UserDAO extends Collection

/**
  * User DAO Companion
  * @author lawrence.daniels@gmail.com
  */
object UserDAO {

  /**
    * User DAO Enrichment
    * @param userDAO the given [[UserDAO User DAO]]
    */
  implicit class UserDAOEnrichment(val userDAO: UserDAO) extends AnyVal {

    @inline
    def findAndUpdateByEmail(username: String, primaryEmail: String)(implicit ec: ExecutionContext) = {
      userDAO.findOneAndUpdate(filter = "primaryEmail" $regex(primaryEmail, ignoreCase = true), update = $set("username" -> username),
        options = new FindAndUpdateOptions(returnOriginal = false)).toFuture map {
        case result if result.isOk => result.valueAs[User]
        case result => die("Account not found")
      }
    }

    @inline
    def findByEmail(primaryEmail: String)(implicit ec: ExecutionContext) = {
      userDAO.findOneFuture[User]("primaryEmail" $regex(primaryEmail, ignoreCase = true))
    }

    @inline
    def findByUsername(username: String)(implicit ec: ExecutionContext) = {
      userDAO.findOneFuture[User]("username" $regex(username, ignoreCase = true))
    }

    @inline
    def findFollowers(followeeID: String) = {
      userDAO.find("followers" $in js.Array(followeeID)).toArrayFuture[User]
    }

  }

  /**
    * User DAO Extensions
    * @author db the given [[Db DB instance]]
    */
  implicit class UserDAOExtensions(val db: Db) extends AnyVal {

    @inline
    def getUserDAO(implicit ec: ExecutionContext) = {
      db.collectionFuture("users").mapTo[UserDAO]
    }

  }

}
