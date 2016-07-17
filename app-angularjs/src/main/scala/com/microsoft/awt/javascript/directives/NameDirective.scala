package com.microsoft.awt.javascript.directives

import com.microsoft.awt.javascript.factories.UserFactory
import org.scalajs.angularjs.{Attributes, Directive, JQLite, Scope, injected}
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Name Directive
  * @author lawrence.daniels@gmail.com
  * @example <full-name id="{{ submitter._id }}"></fullName>
  */
class NameDirective(@injected("UserFactory") userFactory: UserFactory) extends Directive[NameDirectiveScope] {
  override val restrict = "E"
  override val scope = new NameScopeTemplate(id = "@id", `class` = "@class", style = "@style")
  override val transclude = true
  override val replace = false
  override val template = "{{ name }}"

  override def link(scope: NameDirectiveScope, element: JQLite, attrs: Attributes) = {
    scope.$watch("id", (newValue: Any, oldValue: Any) => populateScope(scope, newValue, oldValue))
  }

  private def populateScope(scope: NameDirectiveScope, newValue: Any, oldValue: Any) {
    scope.id.flat foreach { id =>
      if(id.nonEmpty) {
        userFactory.getUserByID(id) foreach { user =>
          scope.$apply { () =>
            scope.name = user.fullName
          }
        }
      }
    }
  }

}

/**
  * Name Directive Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait NameDirectiveScope extends Scope {
  // input fields
  var id: js.UndefOr[String] = js.native
  var link: js.UndefOr[String] = js.native
  var `class`: js.UndefOr[String] = js.native
  var style: js.UndefOr[String] = js.native

  // output fields
  var name: String = js.native

}

/**
  * Name Directive Scope Singleton
  * @author lawrence.daniels@gmail.com
  */
object NameDirectiveScope {

  def apply(): NameDirectiveScope = {
    val scope = New[NameDirectiveScope]
    scope.name = null
    scope
  }
}

/**
  * Name Directive Scope Template
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class NameScopeTemplate(val id: String, val `class`: String, val style: String) extends js.Object
