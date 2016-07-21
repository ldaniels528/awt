package com.microsoft.awt.models

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Implemented by Workload-like objects
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
trait WorkloadLike[T] extends js.Object {
  var name: js.UndefOr[String]
  var technologyProduct: js.UndefOr[String]
  var azureServices: js.UndefOr[js.Array[String]]
  var customerSegment: js.UndefOr[String]
  var businessSponsor: js.UndefOr[String]
  var technicalContact: js.UndefOr[String]
  var msftLeadId: js.UndefOr[String]
  var msftLead: js.UndefOr[String]
  var statusCode: js.UndefOr[String]
  var statuses: js.UndefOr[js.Array[T]]
  var deployedStatus: js.UndefOr[String]
  var estimateGoLiveDate: js.UndefOr[js.Date]
  var consumption: js.UndefOr[Double]
  var active: js.UndefOr[Boolean]
  var creationTime: js.UndefOr[js.Date]
  var lastUpdatedTime: js.UndefOr[js.Date]

}

/**
  * Workload Like Companion
  * @author lawrence.daniels@gmail.com
  */
object WorkloadLike {

  /**
    * Workload Like Extensions
    * @param workload the given [[WorkloadLike workload-like]] instance
    */
  implicit class WorkloadLikeExtensions[A](val workload: WorkloadLike[A]) extends AnyVal {

    def asKeyValues = workload.asInstanceOf[js.Dictionary[js.Any]]

  }

}