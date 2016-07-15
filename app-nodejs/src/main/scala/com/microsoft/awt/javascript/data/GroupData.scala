package com.microsoft.awt.javascript.data

import com.microsoft.awt.javascript.models.Group
import org.scalajs.nodejs.mongodb.MongoDB

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.Try

/**
  * Group Data
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class GroupData(var _id: js.UndefOr[String] = js.undefined,
                var name: js.UndefOr[String] = js.undefined,
                var description: js.UndefOr[String] = js.undefined,
                var owner: js.UndefOr[String] = js.undefined,
                var members: js.UndefOr[js.Array[String]] = js.undefined,
                var creationTime: js.UndefOr[js.Date] = js.undefined) extends js.Object

/**
  * Group Data Companion
  * @author lawrence.daniels@gmail.com
  */
object GroupData {

  /**
    * Group Extensions
    * @param model the given [[Group group]]
    */
  implicit class GroupExtensions(val model: Group) extends AnyVal {

    def toData(implicit mongo: MongoDB): Try[GroupData] = Try {
      new GroupData(
        _id = model._id,
        name = model.name,
        description = model.description,
        owner = model.owner,
        members = model.members,
        creationTime = model.creationTime
      )
    }
  }

  /**
    * Group Data Extensions
    * @param data the given [[GroupData group data model]]
    */
  implicit class GroupDataExtensions(val data: GroupData) extends AnyVal {

    def toModel = new Group(
      _id = data._id,
      name = data.name,
      description = data.description,
      owner = data.owner,
      members = data.members,
      creationTime = data.creationTime
    )
  }

}