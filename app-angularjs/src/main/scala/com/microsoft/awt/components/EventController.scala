package com.microsoft.awt.components

import com.microsoft.awt.models.{Agenda, Event}
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.angularjs.{Controller, Timeout, injected}
import org.scalajs.dom.browser.console

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
  * Events Controller (AngularJS)
  * @author lawrence.daniels@gmail.com
  */
class EventController($scope: EventControllerScope, $timeout: Timeout, toaster: Toaster,
                      @injected("EventService") eventService: EventService,
                      @injected("UserService") userService: UserService)
  extends Controller {

  ///////////////////////////////////////////////////////////////////////////
  //      Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.init = () => {
    console.log(s"Initializing '${getClass.getSimpleName}'...")
  }

  $scope.selectAgenda = (anEvent: js.UndefOr[Event], anAgenda: js.UndefOr[Agenda]) => {
    $scope.selectedEvent = anEvent
    $scope.selectedAgenda = anAgenda
  }

  $scope.selectEvent = (anEvent: js.UndefOr[Event]) => {
    $scope.selectedEvent = anEvent
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Event Handlers
  ///////////////////////////////////////////////////////////////////////////

  $scope.$watch("events", (oldEvents: js.Array[js.Array[Event]], newEvents: js.UndefOr[js.Array[Event]]) => {
    $scope.selectedEvent = $scope.events.flatMap(_.headOption.orUndefined)
    $scope.selectedAgenda = $scope.selectedEvent.flatMap(_.agenda.flatMap(_.headOption.orUndefined))
  })

}

/**
  * Event Controller Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait EventControllerScope extends MainScope {
  var selectedAgenda: js.UndefOr[Agenda] = js.native
  var selectedEvent: js.UndefOr[Event] = js.native

  ///////////////////////////////////////////////////////////////////////////
  //      Public Functions
  ///////////////////////////////////////////////////////////////////////////

  //var init: js.Function0[Unit] = js.native
  var selectAgenda: js.Function2[js.UndefOr[Event], js.UndefOr[Agenda], Unit] = js.native
  var selectEvent: js.Function1[js.UndefOr[Event], Unit] = js.native

}

