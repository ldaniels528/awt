package com.microsoft.awt.javascript.forms

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Login Form
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class LoginForm(var username: js.UndefOr[String] = js.undefined,
                var password: js.UndefOr[String] = js.undefined) extends js.Object

/**
  * Login Form Companion
  * @author lawrence.daniels@gmail.com
  */
object LoginForm {

  def apply() = new LoginForm()

  def apply(username: String, password: String): LoginForm = {
    val form = new LoginForm()
    form.username = username
    form.password = password
    form
  }

  /**
    * Login Form Enrichment
    * @param form the given [[LoginForm login form]]
    */
  implicit class LoginFormEnrichment(val form: LoginForm) extends AnyVal {

    def validate = {
      val messages = js.Array[String]()
      if (!form.username.exists(_.nonEmpty)) messages.push("Username is required")
      if (!form.password.exists(_.nonEmpty)) messages.push("Password is required")
      messages
    }

  }

}
