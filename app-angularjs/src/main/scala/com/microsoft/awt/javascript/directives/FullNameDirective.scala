package com.microsoft.awt.javascript.directives

import com.microsoft.awt.javascript.factories.UserFactory
import org.scalajs.angularjs.{Attributes, Directive, JQLite, Scope, injected}
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * FullName Directive
  * @author lawrence.daniels@gmail.com
  * @example <full-name id="{{ submitter._id }}"></fullName>
  */
class FullNameDirective(@injected("UserFactory") userFactory: UserFactory) extends Directive[FullNameDirectiveScope] {
  override val restrict = "E"
  override val scope = new FullNameScopeTemplate(id = "@id", `class` = "@class", style = "@style")
  override val transclude = true
  override val replace = false
  override val template = "{{ firstName }} {{ lastName }}"

  override def link(scope: FullNameDirectiveScope, element: JQLite, attrs: Attributes) = {
    scope.$watch("id", (newValue: Any, oldValue: Any) => populateScope(scope, newValue, oldValue))
  }

  private def populateScope(scope: FullNameDirectiveScope, newValue: Any, oldValue: Any) {
    // determine the image URL
    val result = (for {
      id <- scope.id.toOption
      user <- userFactory.findUserByID(id)
    } yield user).orUndefined

    scope.firstName = result.flatMap(_.firstName).orNull
    scope.lastName = result.flatMap(_.lastName).orNull
  }

}

/**
  * FullName Directive Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait FullNameDirectiveScope extends Scope {
  // input fields
  var id: js.UndefOr[String] = js.native
  var link: js.UndefOr[String] = js.native
  var `class`: js.UndefOr[String] = js.native
  var style: js.UndefOr[String] = js.native

  // output fields
  var firstName: String = js.native
  var lastName: String = js.native

}

/**
  * FullName Directive Scope Singleton
  * @author lawrence.daniels@gmail.com
  */
object FullNameDirectiveScope {

  def apply(): FullNameDirectiveScope = {
    val scope = New[FullNameDirectiveScope]
    scope.firstName = null
    scope.lastName = null
    scope
  }
}

/**
  * FullName Directive Scope Template
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class FullNameScopeTemplate(val id: String, val `class`: String, val style: String) extends js.Object

