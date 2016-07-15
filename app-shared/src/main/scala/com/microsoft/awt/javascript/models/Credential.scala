package com.microsoft.awt.javascript.models

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents an Authentication Credential
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class Credential(var _id: js.UndefOr[String] = js.undefined,
                 var username: js.UndefOr[String] = js.undefined,
                 var creationTime: js.UndefOr[js.Date] = js.undefined) extends js.Object