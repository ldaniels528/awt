package com.microsoft.awt.components

import org.scalajs.angularjs.{Controller, Scope}
import org.scalajs.dom.browser.console

import scala.scalajs.js

/**
  * Verification Controller
  * @author lawrence.daniels@gmail.com
  */
class VerificationController($scope: VerificationScope, $routeParams: VerificationRouteParams) extends Controller {

  $scope.init = () => {
    console.log(s"Initializing '${getClass.getSimpleName}'...")
  }

}

/**
  * Verification Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait VerificationScope extends Scope {
  // variables
  var primaryEmail: js.UndefOr[String] = js.native
  var code: js.UndefOr[String] = js.native

  // functions
  var init: js.Function0[Unit] = js.native
}

/**
  * Verification Route Parameters
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait VerificationRouteParams extends Scope {
  var primaryEmail: js.UndefOr[String] = js.native

}