package com.microsoft.awt.javascript.controllers

import org.scalajs.angularjs.{Controller, Scope, Timeout}
import org.scalajs.dom.browser.console
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js

/**
  * Global Loading Feature
  * @author lawrence.daniels@gmail.com
  */
trait GlobalLoading {
  self: Controller =>

  def $scope: GlobalLoadingScope

  def $timeout: Timeout

  ///////////////////////////////////////////////////////////////////////////
  //      Global Loading Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.isLoading = () => $scope.loading.contains(true)

  $scope.loadingDelayedStop = (delay: Int) => {
    $timeout(() => $scope.loadingStop(), delay)
  }

  $scope.loadingStart = () => {
    console.log("Loading animation triggered...")
    $scope.loading = true
  }

  $scope.loadingStop = () => {
    console.log("Loading animation stopped.")
    $scope.loading = false
  }

}

/**
  * Global Loading Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait GlobalLoadingScope extends Scope {
  var loading: js.UndefOr[Boolean] = js.native

  var isLoading: js.Function0[Boolean] = js.native
  var loadingDelayedStop: js.Function1[Int, js.Promise[js.Any]] = js.native
  var loadingStart: js.Function0[Unit] = js.native
  var loadingStop: js.Function0[Unit] = js.native

}