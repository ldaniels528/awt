package com.microsoft.awt.directives

import com.microsoft.awt.components.UserFactory
import org.scalajs.angularjs.Directive._
import org.scalajs.angularjs._
import org.scalajs.nodejs.util.ScalaJsHelper._
import org.scalajs.sjs.JsUnderOrHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Avatar Directive
  * @author lawrence.daniels@gmail.com
  * @example <avatar id="{{submitter._id }}" class="avatar-24"></avatar>
  */
class AvatarDirective(@injected("UserFactory") userFactory: UserFactory) extends Directive
  with ElementRestriction with LinkSupport[AvatarDirectiveScope] with TemplateSupport {

  private val LOADING_SPINNER = "/assets/images/status/loading16.gif"
  private val UNKNOWN_PERSON = "/assets/images/avatars/anonymous.png"

  override val scope = AvatarDirectiveScope(id = "@id", named = "@named", `class` = "@class", style = "@style")
  override val template = s"""<img ng-src="{{ url }}" class="{{ class }}" style="{{ style }}"> {{ name }}"""

  override def link(scope: AvatarDirectiveScope, element: JQLite, attrs: Attributes) = {
    scope.$watch("id", (newId: js.UndefOr[String], oldId: js.UndefOr[String]) => {
      newId.flat.toOption.map(_.trim) match {
        case Some(userID) if userID.nonEmpty => loadUserByID(scope, userID)
        case _ => scope.url = UNKNOWN_PERSON
      }
    })
  }

  /**
    * Retrieves a user by ID
    * @param userID the given user ID
    */
  private def loadUserByID(scope: AvatarDirectiveScope, userID: String) = {
    scope.url = LOADING_SPINNER
    if (scope.named.isAssigned) scope.name = "Loading..."

    userFactory.getUserByID(userID) onComplete {
      case Success(user) =>
        scope.$apply { () =>
          if (scope.named.isAssigned) scope.name = user.fullName
          scope.url = user.avatarURL.flat getOrElse UNKNOWN_PERSON
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
  * Avatar Directive Scope Companion
  * @author lawrence.daniels@gmail.com
  */
object AvatarDirectiveScope {

  def apply(id: String, named: String, `class`: String, style: String) = {
    val scope = New[AvatarDirectiveScope]
    scope.id = id
    scope.named = named
    scope.`class` = `class`
    scope.style = style
    scope
  }

}
