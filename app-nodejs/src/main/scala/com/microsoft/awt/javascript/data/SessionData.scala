package com.microsoft.awt.javascript.data

import org.scalajs.nodejs.mongodb.ObjectID

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents session data
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class SessionData(var _id: js.UndefOr[ObjectID] = js.undefined,
                  var userID: js.UndefOr[String] = js.undefined,
                  var username: js.UndefOr[String] = js.undefined,
                  var primaryEmail: js.UndefOr[String] = js.undefined,
                  var avatarURL: js.UndefOr[String] = js.undefined,
                  var isAnonymous: js.UndefOr[Boolean] = js.undefined,
                  var creationTime: js.UndefOr[js.Date] = js.undefined,
                  var lastUpdated: js.UndefOr[Double] = js.undefined) extends js.Object

