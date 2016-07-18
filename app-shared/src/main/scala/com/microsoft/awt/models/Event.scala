package com.microsoft.awt.models

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents an event model object
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class Event(var _id: js.UndefOr[String] = js.undefined,
            var `type`: js.UndefOr[String] = js.undefined,
            var title: js.UndefOr[String] = js.undefined,
            var agenda: js.UndefOr[js.Array[Agenda]] = js.undefined,
            var address: js.UndefOr[String] = js.undefined,
            var city: js.UndefOr[String] = js.undefined,
            var state: js.UndefOr[String] = js.undefined,
            var country: js.UndefOr[String] = js.undefined,
            var avatarId: js.UndefOr[String] = js.undefined,
            var avatarURL: js.UndefOr[String] = js.undefined,
            var ownerId: js.UndefOr[String] = js.undefined,
            var participantIds: js.UndefOr[js.Array[String]] = js.undefined,
            var startTime: js.UndefOr[js.Date] = js.undefined,
            var endTime: js.UndefOr[js.Date] = js.undefined,
            var creationTime: js.UndefOr[js.Date] = js.undefined) extends js.Object

/**
  * Represents an event agenda model object
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class Agenda(var title: js.UndefOr[String] = js.undefined,
             var activities: js.UndefOr[js.Array[Agenda.Activity]] = js.undefined) extends js.Object

/**
  * Agenda Companion
  * @author lawrence.daniels@gmail.com
  */
object Agenda {

  /**
    * Represents an agenda activity model object
    */
  @ScalaJSDefined
  class Activity(var name: js.UndefOr[String] = js.undefined,
                 var description: js.UndefOr[String] = js.undefined) extends js.Object

}