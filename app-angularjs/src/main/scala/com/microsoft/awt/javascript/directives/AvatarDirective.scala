package com.microsoft.awt.javascript.directives

import com.microsoft.awt.javascript.directives.AvatarDirective._
import com.microsoft.awt.javascript.factories.UserFactory
import org.scalajs.angularjs._
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Avatar Directive
  * @author lawrence.daniels@gmail.com
  * @example <avatar id="{{submitter._id }}" class="avatar-24"></avatar>
  */
class AvatarDirective(@injected("UserFactory") userFactory: UserFactory) extends Directive[AvatarDirectiveScope] {
  override val restrict = "E"
  override val scope = new AvatarScopeTemplate(id = "@id", `class` = "@class", style = "@style")
  override val transclude = true
  override val replace = false
  override val template = """<img ng-src="{{ url }}" class="{{ class }}" style="{{ style }}">"""

  override def link(scope: AvatarDirectiveScope, element: JQLite, attrs: Attributes) = {
    scope.$watch("id", (newValue: Any, oldValue: Any) => populateScope(scope, newValue, oldValue))
  }

  private def populateScope(scope: AvatarDirectiveScope, newValue: Any, oldValue: Any) {
    // determine the image URL
    val result = for {
      id <- scope.id.toOption
      user <- userFactory.findUserByID(id)
      avatarURL <- user.avatarURL.toOption
    } yield avatarURL

    scope.url = result getOrElse UNKNOWN_PERSON
  }

}

/**
  * Avatar Directive Singleton
  * @author lawrence.daniels@gmail.com
  */
object AvatarDirective {
  private val UNKNOWN_PERSON = "/assets/images/avatars/anonymous.png"

}

/**
  * Avatar Directive Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait AvatarDirectiveScope extends Scope {
  // input fields
  var id: js.UndefOr[String] = js.native
  var link: js.UndefOr[String] = js.native
  var `class`: js.UndefOr[String] = js.native
  var style: js.UndefOr[String] = js.native

  // output fields
  var url: String = js.native

}

/**
  * Avatar Directive Scope Singleton
  * @author lawrence.daniels@gmail.com
  */
object AvatarDirectiveScope {

  def apply(): AvatarDirectiveScope = {
    val scope = New[AvatarDirectiveScope]
    scope.url = null
    scope
  }
}

/**
  * Avatar Directive Scope Template
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class AvatarScopeTemplate(val id: String, val `class`: String, val style: String) extends js.Object
