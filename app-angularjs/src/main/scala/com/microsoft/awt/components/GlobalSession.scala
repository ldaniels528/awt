package com.microsoft.awt.components

import com.microsoft.awt.models.{Session, User}
import org.scalajs.angularjs.{Controller, Scope}

import scala.scalajs.js

/**
  * Global Session
  * @author lawrence.daniels@gmail.com
  */
trait GlobalSession {
  self: Controller =>

  def $scope: GlobalSessionScope

  def sessionFactory: SessionFactory

  ///////////////////////////////////////////////////////////////////////////
  //      User & Session Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.session = () => sessionFactory.session

  $scope.user = () => sessionFactory.user

}

/**
  * Global Session Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait GlobalSessionScope extends Scope {
  var session: js.Function0[js.UndefOr[Session]] = js.native
  var user: js.Function0[js.UndefOr[User]] = js.native

}