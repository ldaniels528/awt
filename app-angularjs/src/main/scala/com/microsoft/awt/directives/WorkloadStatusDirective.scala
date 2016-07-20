package com.microsoft.awt.directives

import org.scalajs.angularjs.{Attributes, Directive, JQLite, Scope}
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Workload Status Directive
  * @author lawrence.daniels@gmail.com
  * @example <workload-status code="GREEN"></workload-status>
  */
class WorkloadStatusDirective() extends Directive[WorkloadStatusDirectiveScope] {
  override val restrict = "E"
  override val scope = new WorkloadStatusScopeTemplate(code = "@code", labeled = "@labeled")
  override val transclude = true
  override val replace = false
  override val template =
    """
      <i ng-class="iconClass"></i>
      <span ng-show="labeled" ng-class="iconTextClass">{{ iconText }}</span>
    """

  override def link(scope: WorkloadStatusDirectiveScope, element: JQLite, attrs: Attributes) = {
    scope.$watch("code", (newValue: js.UndefOr[String], oldValue: js.UndefOr[String]) => {
      val value = scope.code.flat
      scope.iconClass = getStatusIcon(value).orNull
      scope.iconTextClass = getStatusClass(value).orNull
      scope.iconText = value.map(_.toUpperCase()).orNull
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
  var iconClass: String = js.native
  var iconTextClass: String = js.native
  var iconText: String = js.native

}

/**
  * Workload Status Directive Scope Template
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class WorkloadStatusScopeTemplate(val code: String, val labeled: String) extends js.Object

