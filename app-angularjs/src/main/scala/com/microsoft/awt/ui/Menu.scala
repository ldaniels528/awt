package com.microsoft.awt.ui

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a Menu
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class Menu(val text: String,
           val items: js.Array[MenuItem],
           val link: js.UndefOr[String] = js.undefined) extends js.Object
