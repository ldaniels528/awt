package com.microsoft.awt.models

import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a group model object
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class Group(var _id: js.UndefOr[String] = js.undefined,
            var name: js.UndefOr[String] = js.undefined,
            var description: js.UndefOr[String] = js.undefined,
            var owner: js.UndefOr[String] = js.undefined,
            var members: js.UndefOr[js.Array[String]] = js.undefined,
            var creationTime: js.UndefOr[js.Date] = js.undefined) extends js.Object {

  // UI element members
  var memberUsers: js.UndefOr[js.Array[User]] = js.undefined
  var workloads: js.UndefOr[js.Array[Workload]] = js.undefined
  var expanded: js.UndefOr[Boolean] = js.undefined
  var loading: js.UndefOr[Boolean] = js.undefined

}

/**
  * Group Companion
  * @author lawrence.daniels@gmail.com
  */
object Group {

  /**
    * Group Enrichment
    * @param group the given [[Group group]]
    */
  implicit class GroupEnrichment(val group: Group) extends AnyVal {

    /**
      * @return true, if the group is expanded
      */
    @inline
    def isExpanded = group.expanded.contains(true)

    /**
      * Expands/collapses the group
      */
    @inline
    def toggle() = group.expanded = !group.expanded.contains(true)

  }

}