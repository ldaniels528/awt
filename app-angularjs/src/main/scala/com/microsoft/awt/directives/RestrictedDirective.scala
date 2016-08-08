package com.microsoft.awt.directives

import com.microsoft.awt.components.SessionFactory
import org.scalajs.angularjs.Directive._
import org.scalajs.angularjs.{Attributes, Directive, JQLite, Scope, injected}

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Restricted Directive
  * @author lawrence.daniels@gmail.com
  */
class RestrictedDirective(@injected("SessionFactory") sessionFactory: SessionFactory) extends Directive
  with ElementSupport with LinkSupport[RestrictedDirectiveScope] with TemplateSupport {

  override val scope = new RestrictedDirectiveScopeTemplate(`class` = "@class", style = "@style")
  override val transclude = true

  override def template =
    """
    <span ng-if="isAnonymous" class="sk_restricted {{ class }}" style="{{ style }}">
      <i class="fa fa-user-secret"></i> <span style="font-size: 9pt">Restricted</span>
    </span>
    <span ng-if="!isAnonymous" ng-transclude></span>
    """

  override def link(scope: RestrictedDirectiveScope, element: JQLite, attrs: Attributes): Unit = {
    scope.isAnonymous = sessionFactory.session.flatMap(_.isAnonymous)
  }

}

/**
  * Restricted Directive Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait RestrictedDirectiveScope extends Scope {
  // input variables
  var `class`: js.UndefOr[String] = js.native
  var style: js.UndefOr[String] = js.native

  // output variables
  var isAnonymous: js.UndefOr[Boolean] = js.native
}

/**
  * Restricted Directive Scope Template
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class RestrictedDirectiveScopeTemplate(var `class`: js.UndefOr[String], var style: js.UndefOr[String]) extends js.Object