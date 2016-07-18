package com.microsoft.awt.forms

import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Account Activation Form
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class AccountActivationForm(var username: js.UndefOr[String] = js.undefined,
                            var primaryEmail: js.UndefOr[String] = js.undefined,
                            var password0: js.UndefOr[String] = js.undefined,
                            var password1: js.UndefOr[String] = js.undefined) extends js.Object

/**
  * Account Activation Form Companion
  * @author lawrence.daniels@gmail.com
  */
object AccountActivationForm {

  /**
    * Account Activation Form Enrichment
    * @param form the given [[AccountActivationForm form]]
    */
  implicit class AccountActivationFormEnrichment(val form: AccountActivationForm) extends AnyVal {

    @inline
    def copy(username: js.UndefOr[String] = js.undefined,
             primaryEmail: js.UndefOr[String] = js.undefined,
             password0: js.UndefOr[String] = js.undefined,
             password1: js.UndefOr[String] = js.undefined) = {
      new AccountActivationForm(
        username = username ?? form.username,
        primaryEmail = primaryEmail ?? form.primaryEmail,
        password0 = password0 ?? form.password0,
        password1 = password1 ?? form.password1
      )
    }

    @inline
    def validate(): js.Array[String] = {
      val messages = js.Array[String]()
      if (!form.username.flat.exists(_.trim.nonEmpty)) messages.push("Username is required")
      if (!form.password0.flat.exists(_.trim.nonEmpty)) messages.push("Password is required")
      if (!form.password1.flat.exists(_.trim.nonEmpty)) messages.push("Confirmation password is required")
      if (form.password0 ?!= form.password1) messages.push("The passwords do not match")
      if (!form.primaryEmail.exists(_.trim.nonEmpty)) messages.push("Email address is required")
      if (form.primaryEmail.exists(s => s.trim.nonEmpty && !isValidEmail(s))) messages.push("Email Address is invalid")
      messages
    }

    @inline
    def isValidEmail(emailAddress: String): Boolean = {
      emailAddress.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
    }

  }

}