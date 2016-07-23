package com.microsoft.awt.directives

import com.microsoft.awt.components.UserFactory
import org.scalajs.angularjs._
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Avatar Directive
  * @author lawrence.daniels@gmail.com
  * @example <avatar id="{{submitter._id }}" class="avatar-24"></avatar>
  */
class AvatarDirective(@injected("UserFactory") userFactory: UserFactory) extends Directive[AvatarDirectiveScope] {
  private val UNKNOWN_PERSON = "/assets/images/avatars/anonymous.png"

  override val restrict = "E"
  override val scope = new AvatarScopeTemplate(id = "@id", named = "@named", `class` = "@class", style = "@style")
  override val transclude = true
  override val replace = false
  override val template = """<img ng-src="{{ url }}" class="{{ class }}" style="{{ style }}"> {{ name }}"""

  override def link(scope: AvatarDirectiveScope, element: JQLite, attrs: Attributes) = {
    scope.$watch("id", (newValue: js.UndefOr[String], oldValue: js.UndefOr[String]) => {
      newValue.flat foreach {
        case userID if userID.nonEmpty =>
          scope.url = "/assets/images/status/loading16.gif"
          if (scope.named.isAssigned) scope.name = "Loading..."

          userFactory.getUserByID(userID) foreach { user =>
            scope.$apply { () =>
              scope.url = user.avatarURL getOrElse UNKNOWN_PERSON
              if (scope.named.isAssigned) scope.name = user.fullName
            }
          }
        case _ =>
      }
    })
  }

}

/**
  * Avatar Directive Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait AvatarDirectiveScope extends Scope {
  // input fields
  var id: js.UndefOr[String] = js.native
  var named: js.UndefOr[String] = js.native
  var `class`: js.UndefOr[String] = js.native
  var style: js.UndefOr[String] = js.native

  // output fields
  var name: String = js.native
  var url: String = js.native

}

/**
  * Avatar Directive Scope Template
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class AvatarScopeTemplate(val id: js.UndefOr[String],
                          val named: js.UndefOr[String],
                          val `class`: js.UndefOr[String],
                          val style: js.UndefOr[String]) extends js.Object
