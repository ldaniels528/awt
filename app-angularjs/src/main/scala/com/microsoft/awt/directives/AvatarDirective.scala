package com.microsoft.awt.directives

import com.microsoft.awt.components.UserFactory
import org.scalajs.angularjs.Directive._
import org.scalajs.angularjs._
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.{Failure, Success}

/**
  * Avatar Directive
  * @author lawrence.daniels@gmail.com
  * @example <avatar id="{{submitter._id }}" class="avatar-24"></avatar>
  */
class AvatarDirective(@injected("UserFactory") userFactory: UserFactory) extends Directive
  with ElementSupport with LinkSupport[AvatarDirectiveScope] with TemplateSupport {

  private val LOADING_SPINNER = "/assets/images/status/loading16.gif"
  private val UNKNOWN_PERSON = "/assets/images/avatars/anonymous.png"

  override val replace = false
  override val scope = new AvatarScopeTemplate(id = "@id", named = "@named", `class` = "@class", style = "@style")
  override val template = s"""<img ng-src="{{ url }}" class="{{ class }}" style="{{ style }}"> {{ name }}"""
  override val transclude = true

  override def link(scope: AvatarDirectiveScope, element: JQLite, attrs: Attributes) = {
    scope.id.flat foreach {
      case userID if userID.nonEmpty =>
        scope.url = LOADING_SPINNER
        if (scope.named.isAssigned) scope.name = "Loading..."
        loadUserByID(userID)(scope)
      case _ =>
        scope.url = UNKNOWN_PERSON
    }
  }

  private def loadUserByID(userID: String)(implicit scope: AvatarDirectiveScope) = {
    userFactory.getUserByID(userID) onComplete {
      case Success(user) =>
        scope.$apply { () =>
          if (scope.named.isAssigned) scope.name = user.fullName
          scope.url = user.avatarURL getOrElse UNKNOWN_PERSON
        }
      case Failure(e) =>
        scope.$apply { () =>
          scope.name = "Error"
          scope.url = UNKNOWN_PERSON
        }
    }
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
