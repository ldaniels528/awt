package com.microsoft.awt.components

import org.scalajs.angularjs.AngularJsHelper._
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.angularjs.{Controller, Scope}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Global Authorization
  * @author lawrence.daniels@gmail.com
  */
trait GlobalAuthorization extends GlobalLoading {
  self: Controller =>

  def sessionFactory: SessionFactory

  def $scope: GlobalAuthorizationScope

  def toaster: Toaster

  $scope.logout = () => sessionFactory.session.flatMap(_._id) foreach { sessionID =>
    $scope.loadingStart()
    sessionFactory.logout(sessionID) onComplete {
      case Success(result) =>
        if (result.isOk) sessionFactory.logout() else toaster.error("Logout Failure", "")
        $scope.$apply { () => $scope.loadingStop() }
        $scope.navigateToLogin()
      case Failure(e) =>
        toaster.error("Logout Failure", e.displayMessage)
        $scope.$apply { () => $scope.loadingStop() }
    }
  }

}

/**
  * Global Authorization Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait GlobalAuthorizationScope extends Scope with GlobalLoadingScope with GlobalNavigationScope {
  var logout: js.Function0[Unit] = js.native

}