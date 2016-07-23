package com.microsoft.awt

import com.microsoft.awt.models._
import org.scalajs.angularjs.Scope
import org.scalajs.dom
import org.scalajs.dom.browser.console

import scala.scalajs.js

/**
  * components package object
  * @author lawrence.daniels@gmail.com
  */
package object components {

  /**
    * Scope Events
    * @param $scope the given [[Scope scope]]
    */
  implicit class ScopeEvents(val $scope: Scope) extends AnyVal {

    ///////////////////////////////////////////////////////////////////////////
    //     Session Listeners
    ///////////////////////////////////////////////////////////////////////////

    @inline
    def emitSessionLoaded(session: Session) = $scope.$broadcast("session_loaded", session)

    @inline
    def onSessionLoaded(listener: (dom.Event, Session) => Any) = $scope.$on("session_loaded", listener)

    ///////////////////////////////////////////////////////////////////////////
    //      User Listeners
    ///////////////////////////////////////////////////////////////////////////

    @inline
    def emitUserLoaded(user: User) = {
      console.log("emitting user loaded event...")
      $scope.$broadcast("user_loaded", user)
    }

    @inline
    def onUserLoaded(listener: (dom.Event, User) => Any) = {
      console.log("received user loaded event.")
      $scope.$on("user_loaded", listener)
    }

    ///////////////////////////////////////////////////////////////////////////
    //      Workload Listeners
    ///////////////////////////////////////////////////////////////////////////

    @inline
    def emitToggleActiveWorkloads(state: js.UndefOr[Boolean]) = $scope.$emit("toggleActiveWorkloads", state)

    @inline
    def onToggleActiveWorkloads(listener: (dom.Event, Boolean) => Any) = $scope.$on("toggleActiveWorkloads", listener)

    ///////////////////////////////////////////////////////////////////////////
    //      Web Socket Listeners
    ///////////////////////////////////////////////////////////////////////////

    @inline
    def onWsNotificationMessage(listener: (dom.Event, Notification) => Any) =  $scope.$on(WsEventMessage.NOTIFICATION, listener)

    @inline
    def onWsPostMessage(listener: (dom.Event, Post) => Any) = $scope.$on(WsEventMessage.POST, listener)

    @inline
    def emitWsStateChange(changed: js.UndefOr[Boolean]) = $scope.$broadcast("WS_STATE_CHANGE", changed)

    @inline
    def onWsStateChange(listener: (dom.Event, Boolean) => Any) = $scope.$on("WS_STATE_CHANGE", listener)

  }

}
