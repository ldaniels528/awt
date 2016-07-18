package com.microsoft.awt.models

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * WebSocket Event Message
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class WsEventMessage(var action: js.UndefOr[String] = js.undefined,
                     var data: js.UndefOr[String] = js.undefined) extends js.Object

/**
  * WebSocket Event Message Companion
  * @author lawrence.daniels@gmail.com
  */
object WsEventMessage {
  val NOTIFICATION = "NOTIFICATION"
  val POST = "POST"

  def apply(action: String, data: js.Any) = new WsEventMessage(action = action, data = JSON.stringify(data))

}