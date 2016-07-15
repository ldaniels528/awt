package com.microsoft.awt.javascript.models

import com.microsoft.awt.javascript.models.Workload.Status

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a Workload
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class Workload(var _id: js.UndefOr[String] = js.undefined,
               var name: js.UndefOr[String] = js.undefined,
               var technologyProduct: js.UndefOr[String] = js.undefined,
               var azureServices: js.UndefOr[js.Array[String]] = js.undefined,
               var customerSegment: js.UndefOr[String] = js.undefined,
               var businessSponsor: js.UndefOr[String] = js.undefined,
               var technicalContact: js.UndefOr[String] = js.undefined,
               var msftLeadId: js.UndefOr[String] = js.undefined,
               var msftLead: js.UndefOr[String] = js.undefined,
               var statuses: js.UndefOr[js.Array[Status]] = js.undefined,
               var statusCode: js.UndefOr[String] = js.undefined,
               var deployedStatus: js.UndefOr[String] = js.undefined,
               var estimateGoLiveDate: js.UndefOr[js.Date] = js.undefined,
               var monthlyConsumptionEstimate: js.UndefOr[Double] = js.undefined,
               var active: js.UndefOr[Boolean] = js.undefined,
               var creationTime: js.UndefOr[js.Date] = js.undefined,
               var lastUpdatedTime: js.UndefOr[js.Date] = js.undefined) extends WorkloadLike[Status]

/**
  * Workload Comparison
  * @author lawrence.daniels@gmail.com
  */
object Workload {

  /**
    * Represents a Workload Status
    * @author lawrence.daniels@gmail.com
    */
  @ScalaJSDefined
  class Status(var _id: js.UndefOr[String] = js.undefined,
               var submitterId: js.UndefOr[String] = js.undefined,
               var statusText: js.UndefOr[String] = js.undefined,
               var creationTime: js.UndefOr[js.Date] = js.undefined) extends js.Object

}