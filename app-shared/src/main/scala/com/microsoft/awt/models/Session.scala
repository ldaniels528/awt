package com.microsoft.awt.models

import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a user session model object
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class Session(var _id: js.UndefOr[String] = js.undefined,
              var userID: js.UndefOr[String] = js.undefined,
              var username: js.UndefOr[String] = js.undefined,
              var primaryEmail: js.UndefOr[String] = js.undefined,
              var avatarURL: js.UndefOr[String] = js.undefined,
              var isAnonymous: js.UndefOr[Boolean] = js.undefined,
              var creationTime: js.UndefOr[js.Date] = js.undefined,
              var lastUpdated: js.UndefOr[Double] = js.undefined) extends js.Object
