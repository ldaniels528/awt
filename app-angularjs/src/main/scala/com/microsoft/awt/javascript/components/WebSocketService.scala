package com.microsoft.awt.javascript.components

import com.microsoft.awt.javascript.models.{User, WsEventMessage}
import org.scalajs.angularjs._
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.dom
import org.scalajs.dom.console
import org.scalajs.dom.raw.{CloseEvent, ErrorEvent, MessageEvent, WebSocket}

import scala.concurrent.duration._
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.scalajs.js.JSON

/**
  * Web Socket Service
  * @author lawrence.daniels@gmail.com
  */
class WebSocketService($rootScope: Scope, $location: Location, $timeout: Timeout, toaster: Toaster) extends Service {
  private var connectedUser: Option[User] = None
  private var socket: Option[WebSocket] = None

  /**
    * Initializes the service
    */
  def init(user: User) {
    connectedUser = Option(user)
    console.log("Initializing Websocket service...")
    if (js.isUndefined(g.window.WebSocket)) {
      console.log("Using a Mozilla Web Socket")
      g.window.WebSocket = g.window.MozWebSocket
    }

    if (!js.isUndefined(g.window.WebSocket)) connect()
    else
      toaster.info("Your browser does not support Web Sockets.")
  }

  /**
    * Transmits the message to the server via web-socket
    * @param message the given [[WsEventMessage message]] to transmit
    * @return true, if the message was successfully transmitted
    */
  def send(message: WsEventMessage): Boolean = {
    socket match {
      case Some(sock) if sock.readyState == WebSocket.OPEN =>
        sock.send(angular.toJson(message))
        true
      case Some(sock) =>
        console.error(s"Web socket closed: readyState = ${sock.readyState}")
        false
      case None =>
        console.error("Web socket not intitialized")
        false
    }
  }

  /**
    * Establishes a web socket connection
    */
  private def connect() {
    val endpoint = s"ws://${$location.host()}:${$location.port()}/websocket"
    console.log(s"Connecting to web socket endpoint '$endpoint'...")

    // open the connection and setup the handlers
    socket = Option {
      val newSocket = new WebSocket(endpoint)
      newSocket.onopen = (event: dom.Event) => handleOnOpen(event)
      newSocket.onclose = (event: CloseEvent) => handleOnClose(event)
      newSocket.onerror = (event: ErrorEvent) => handleOnError(event)
      newSocket.onmessage = (event: MessageEvent) => handleOnMessage(event)
      newSocket
    }
  }

  /**
    * Sends the current state of the connection
    * @param event the given [[dom.Event event]]
    */
  private def handleOnClose(event: dom.Event) {
    $rootScope.emitWsStateChange(changed = false)
    console.warn("Web socket connection lost")
    $timeout(() => connect(), 15.seconds)
  }

  /**
    * Handles the web socket error events
    * @param event the given web socket [[ErrorEvent error event]]
    */
  private def handleOnError(event: ErrorEvent) {
    console.error(s"Web socket error: ${event.message}")
  }

  /**
    * Handles the incoming web socket message event
    * @param event the given web socket [[MessageEvent message event]]
    */
  private def handleOnMessage(event: MessageEvent) {
    if (event.data != null) {
      val message = JSON.parse(event.data.asInstanceOf[String]).asInstanceOf[WsEventMessage]
      val params = for {
        action <- message.action.toOption
        data <- message.data.toOption
      } yield (action, data)

      params match {
        case Some((action, data)) =>
          console.log(s"Broadcasting action '$action'")
          $rootScope.$broadcast(action, angular.fromJson(data))
        case None =>
          console.warn(s"Message does not contain an action message: ${JSON.stringify(message)}")
      }
    }
    else console.warn(s"Unhandled event received: ${JSON.stringify(event)}")
  }

  /**
    * Sends the current state of the connection
    * @param event the given [[dom.Event event]]
    */
  private def handleOnOpen(event: dom.Event) {
    $rootScope.emitWsStateChange(changed = true)
    console.log("Web socket connection established")
  }

}

