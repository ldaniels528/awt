package com.microsoft.awt.ui

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a Menu Item
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
trait MenuItem extends js.Object {

  def text: String

  def iconClass: String

  def action: js.Function

  def include: String

}

/**
  * Menu Item Companion
  * @author lawrence.daniels@gmail.com
  */
object MenuItem {

  def apply(text: String, iconClass: String, action: js.Function = null) = {
    new StaticMenuItem(text = text, iconClass = iconClass, action = action)
  }

  def include(src: String) = new StaticMenuItem(include = src)

  @ScalaJSDefined
  class StaticMenuItem(val text: String = null,
                       val iconClass: String = null,
                       val action: js.Function = null,
                       val include: String = null) extends MenuItem

}