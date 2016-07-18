package com.microsoft.awt.models

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents an authentication token
  * @param code       the authorization code
  * @param expiration the expiration time of the code
  */
@ScalaJSDefined
class AuthToken(val code: String, val expiration: Double) extends js.Object