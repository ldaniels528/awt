package com.microsoft.awt.data

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Credential Data Object
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class CredentialData(var _id: js.UndefOr[String] = js.undefined,
                     var username: js.UndefOr[String] = js.undefined,
                     var md5Password: js.UndefOr[String] = js.undefined,
                     var creationTime: js.UndefOr[js.Date] = js.undefined) extends js.Object