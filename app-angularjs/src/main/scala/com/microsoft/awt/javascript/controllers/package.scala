package com.microsoft.awt.javascript

import com.microsoft.awt.javascript.models._
import org.scalajs.angularjs.Scope
import org.scalajs.dom

/**
  * controllers package object
  * @author lawrence.daniels@gmail.com
  */
package object controllers {

  /**
    * Scope Events
    * @param $scope the given [[Scope scope]]
    */
  implicit class ScopeEvents(val $scope: Scope) extends AnyVal {

    @inline
    def onSessionLoaded(listener: (dom.Event, Session) => Any) = $scope.$on("session_loaded", listener)

    @inline
    def onUserLoaded(listener: (dom.Event, User) => Any) = $scope.$on("user_loaded", listener)

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
    def onWsStateChange(listener: (dom.Event, Boolean) => Any) = $scope.$on("WS_STATE_CHANGE", listener)

  }

}
