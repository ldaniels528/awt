package com.microsoft.awt.directives

import com.microsoft.awt.components.UserFactory
import org.scalajs.angularjs.Directive._
import org.scalajs.angularjs.{Attributes, Directive, JQLite, Scope, injected}
import org.scalajs.nodejs.util.ScalaJsHelper._
import org.scalajs.sjs.JsUnderOrHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js

/**
  * Full Name Directive
  * @author lawrence.daniels@gmail.com
  * @example <full-name id="{{ submitter._id }}"></fullName>
  */
class FullNameDirective(@injected("UserFactory") userFactory: UserFactory) extends Directive
  with ElementRestriction with LinkSupport[FullNameDirectiveScope] with TemplateSupport {

  override val scope = FullNameDirectiveScope(id = "@id", `class` = "@class", style = "@style")
  override val template = "{{ name }}"

  override def link(scope: FullNameDirectiveScope, element: JQLite, attrs: Attributes) = {
    scope.$watch("id", (newId: js.UndefOr[String], oldId: js.UndefOr[String]) => {
      newId.flat foreach { id =>
        if (id.nonEmpty) {
          userFactory.getUserByID(id) foreach { user =>
            scope.$apply(() => scope.name = user.fullName)
          }
        }
      }
    })
  }

}

/**
  * Full Name Directive Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait FullNameDirectiveScope extends Scope {
  // input fields
  var id: js.UndefOr[String] = js.native
  var `class`: js.UndefOr[String] = js.native
  var style: js.UndefOr[String] = js.native

  // output fields
  var name: String = js.native

}

/**
  * Full Name Directive Scope Companion
  * @author lawrence.daniels@gmail.com
  */
object FullNameDirectiveScope {

  def apply(id: String, `class`: String, style: String) = {
    val scope = New[FullNameDirectiveScope]
    scope.id = id
    scope.`class` = `class`
    scope.style = style
    scope
  }

}

