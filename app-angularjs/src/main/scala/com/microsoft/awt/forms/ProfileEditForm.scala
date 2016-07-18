package com.microsoft.awt.forms

import com.microsoft.awt.models.User

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Profile Edit Form
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class ProfileEditForm(var _id: js.UndefOr[String] = js.undefined,
                      var firstName: js.UndefOr[String] = js.undefined,
                      var lastName: js.UndefOr[String] = js.undefined,
                      var title: js.UndefOr[String] = js.undefined,
                      var primaryEmail: js.UndefOr[String] = js.undefined,
                      var avatarURL: js.UndefOr[String] = js.undefined) extends js.Object

/**
  * Edit Profile Form Companion
  * @author lawrence.daniels@gmail.com
  */
object ProfileEditForm {

  def apply(aUser: js.UndefOr[User]): ProfileEditForm = {
    val form = new ProfileEditForm()
    aUser foreach { user =>
      form._id = user._id
      form.firstName = user.firstName
      form.lastName = user.lastName
      form.primaryEmail = user.primaryEmail
      form.title = user.title
      form.avatarURL = user.avatarURL
    }
    form
  }

}