package com.microsoft.awt.data

import java.lang.{Boolean => JBoolean}

import com.microsoft.awt.models.{Submitter, User}
import org.scalajs.nodejs.mongodb.{MongoDB, ObjectID}
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.Try

/**
  * Represents a user data model object
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class UserData(var _id: js.UndefOr[ObjectID] = js.undefined,
               var username: js.UndefOr[String] = js.undefined,
               var firstName: js.UndefOr[String] = js.undefined,
               var lastName: js.UndefOr[String] = js.undefined,
               var title: js.UndefOr[String] = js.undefined,
               var primaryEmail: js.UndefOr[String] = js.undefined,
               var avatarId: js.UndefOr[String] = js.undefined,
               var avatarURL: js.UndefOr[String] = js.undefined,
               var endorsements: js.UndefOr[Integer] = js.undefined,
               var endorsers: js.UndefOr[js.Array[String]] = js.undefined,
               var totalFollowers: js.UndefOr[Integer] = js.undefined,
               var followers: js.UndefOr[js.Array[String]] = js.undefined,
               var isAdmin: js.UndefOr[JBoolean] = js.undefined,
               var creationTime: js.UndefOr[js.Date] = js.undefined) extends js.Object

/**
  * User Data Companion
  * @author lawrence.daniels@gmail.com
  */
object UserData {

  /**
    * User Extensions
    * @param model the given [[User user]]
    */
  implicit class UserExtensions(val model: User) extends AnyVal {

    def toData(implicit mongo: MongoDB): Try[UserData] = Try {
      new UserData(
        _id = model._id.map(mongo.ObjectID(_)) ?? mongo.ObjectID(),
        username = model.username,
        firstName = model.firstName,
        lastName = model.lastName,
        title = model.title,
        primaryEmail = model.primaryEmail,
        avatarId = model.avatarId,
        avatarURL = model.avatarURL,
        endorsements = model.endorsements,
        endorsers = model.endorsers,
        totalFollowers = model.totalFollowers,
        followers = model.followers,
        isAdmin = model.isAdmin ?? JBoolean.FALSE,
        creationTime = model.creationTime ?? new js.Date()
      )
    }

  }

  /**
    * User Data Extensions
    * @param data the given [[UserData user data model]]
    */
  implicit class UserDataExtensions(val data: UserData) extends AnyVal {

    def fullName = Seq(data.firstName.toOption, data.lastName.toOption).flatten.mkString(" ")

    def toModel = new User(
      _id = data._id.map(_.toHexString()),
      username = data.username,
      firstName = data.firstName,
      lastName = data.lastName,
      title = data.title,
      primaryEmail = data.primaryEmail,
      avatarId = data.avatarId,
      avatarURL = data.avatarURL,
      endorsements = data.endorsements,
      endorsers = data.endorsers,
      totalFollowers = data.totalFollowers,
      followers = data.followers,
      isAdmin = data.isAdmin ?? JBoolean.FALSE,
      creationTime = data.creationTime ?? new js.Date()
    )

    def toSubmitter = new Submitter(
      _id = data._id.map(_.toHexString()),
      name = data.fullName,
      avatarURL = data.avatarURL
    )

  }

}