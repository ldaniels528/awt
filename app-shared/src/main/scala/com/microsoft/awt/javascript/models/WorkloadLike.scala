package com.microsoft.awt.javascript.models

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

  def toCSVArray[T](workloads: js.Array[_ <: WorkloadLike[T]]) = {
    val headings = workloads.headOption.map(_.asInstanceOf[js.Dictionary[Any]]).map(_.keys.filterNot(_ == "_id").toList) getOrElse Nil
    val data = workloads.map { workload =>
      val dict = workload.asInstanceOf[js.Dictionary[js.Any]]
      js.Array(headings.map(k => asString(dict.get(k).orNull)): _*)
    }
    data.prepend(js.Array(headings: _*))
    data
  }

  private def asString(value: Any) = if (value == null) "" else value.toString

  /**
    * Workload Like Extensions
    * @param workload the given [[WorkloadLike workload-like]] instance
    */
  implicit class WorkloadLikeExtensions[A](val workload: WorkloadLike[A]) extends AnyVal {

  }

}