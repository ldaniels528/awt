package com.microsoft.awt.data

import org.scalajs.nodejs.mongodb._

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Credential DAO
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait CredentialDAO extends Collection

/**
  * Credential DAO Companion
  * @author lawrence.daniels@gmail.com
  */
object CredentialDAO {

  /**
    * Credential DAO Extensions
    * @param credentialDAO the given [[CredentialDAO Credential DAO]]
    */
  implicit class CredentialDAOEnrichment(val credentialDAO: CredentialDAO) extends AnyVal {

    @inline
    def findByUsername(username: String)(implicit ec: ExecutionContext) = {
      credentialDAO.findOneFuture[CredentialData]("username" $eq username)
    }

  }

  /**
    * Credential DAO Extensions
    * @param db the given [[Db database]]
    */
  implicit class CredentialDAOExtensions(val db: Db) extends AnyVal {

    @inline
    def getCredentialDAO(implicit ec: ExecutionContext) = {
      db.collectionFuture("credentials").mapTo[CredentialDAO]
    }

  }

}