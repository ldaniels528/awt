package com.microsoft.awt.directives

import org.scalajs.angularjs.Directive._
import org.scalajs.angularjs.{Attributes, Directive, JQLite, Scope}
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js

/**
  * Workload Status Directive
  * @author lawrence.daniels@gmail.com
  * @example <workload-status code="GREEN"></workload-status>
  */
class WorkloadStatusDirective() extends Directive
  with ElementRestriction with LinkSupport[WorkloadStatusDirectiveScope] with TemplateSupport {

  override val scope = WorkloadStatusScope(code = "@code", labeled = "@labeled")
  override val template =
    """
      <i ng-class="iconClass"></i>
      <span ng-show="labeled" ng-class="iconTextClass">{{ iconText }}</span>
    """

  override def link(scope: WorkloadStatusDirectiveScope, element: JQLite, attrs: Attributes) = {
    scope.$watch("code", (newCode: js.UndefOr[String], oldCode: js.UndefOr[String]) => {
      val value = newCode.flat
      scope.iconClass = getStatusIcon(value)
      scope.iconTextClass = getStatusClass(value)
      scope.iconText = value.map(_.toUpperCase())
    })
  }

  private def getStatusIcon(aStatusCode: js.UndefOr[String]) = aStatusCode map {
    case "GREEN" => "fa fa-battery-4 status_green"
    case "RED" => "fa fa-battery-1 status_red"
    case "YELLOW" => "fa fa-battery-2 status_yellow"
    case _ => "fa fa-battery-0 status_unknown"
  }

  private def getStatusClass(aStatusCode: js.UndefOr[String]) = aStatusCode map {
    case "GREEN" => "status_green"
    case "RED" => "status_red"
    case "YELLOW" => "status_yellow"
    case _ => "status_unknown"
  }

}

/**
  * Workload Status Directive Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait WorkloadStatusDirectiveScope extends Scope {
  // input fields
  var code: js.UndefOr[String] = js.native
  var labeled: js.UndefOr[String] = js.native

  // output fields
  var iconClass: js.UndefOr[String] = js.native
  var iconTextClass: js.UndefOr[String] = js.native
  var iconText: js.UndefOr[String] = js.native

}

/**
  * Workload Status Directive Scope Template
  * @author lawrence.daniels@gmail.com
  */
object WorkloadStatusScope {

  def apply(code: String, labeled: String) = {
    val scope = New[WorkloadStatusDirectiveScope]
    scope.code = code
    scope.labeled = labeled
    scope
  }

}

