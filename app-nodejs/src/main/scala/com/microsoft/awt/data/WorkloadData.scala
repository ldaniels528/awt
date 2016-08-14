package com.microsoft.awt.data

import com.microsoft.awt.data.WorkloadData.StatusData
import com.microsoft.awt.models.Workload.Status
import com.microsoft.awt.models.{Workload, WorkloadLike}
import org.scalajs.nodejs.mongodb.{MongoDB, ObjectID}
import org.scalajs.sjs.JsUnderOrHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.Try

/**
  * Represents a Workload Data Document
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class WorkloadData(var _id: js.UndefOr[ObjectID] = js.undefined,
                   var name: js.UndefOr[String] = js.undefined,
                   var technologyProduct: js.UndefOr[String] = js.undefined,
                   var azureServices: js.UndefOr[js.Array[String]] = js.undefined,
                   var customerSegment: js.UndefOr[String] = js.undefined,
                   var businessSponsor: js.UndefOr[String] = js.undefined,
                   var technicalContact: js.UndefOr[String] = js.undefined,
                   var msftLeadId: js.UndefOr[String] = js.undefined,
                   var msftLead: js.UndefOr[String] = js.undefined,
                   var statuses: js.UndefOr[js.Array[StatusData]] = js.undefined,
                   var statusCode: js.UndefOr[String] = js.undefined,
                   var deployedStatus: js.UndefOr[String] = js.undefined,
                   var estimateGoLiveDate: js.UndefOr[js.Date] = js.undefined,
                   var consumption: js.UndefOr[Double] = js.undefined,
                   var active: js.UndefOr[Boolean] = js.undefined,
                   var creationTime: js.UndefOr[js.Date] = js.undefined,
                   var lastUpdatedTime: js.UndefOr[js.Date] = js.undefined) extends WorkloadLike[StatusData]

/**
  * Workload Data Companion
  * @author lawrence.daniels@gmail.com
  */
object WorkloadData {

  /**
    * Represents a Workload Status
    * @author lawrence.daniels@gmail.com
    */
  @ScalaJSDefined
  class StatusData(var _id: js.UndefOr[ObjectID] = js.undefined,
                   var submitterId: js.UndefOr[String] = js.undefined,
                   var statusText: js.UndefOr[String] = js.undefined,
                   var creationTime: js.UndefOr[js.Date] = js.undefined) extends js.Object

  /**
    * Status Extensions
    * @param model the given [[Status status]]
    */
  implicit class StatusExtensions(val model: Status) extends AnyVal {

    def toData(implicit mongo: MongoDB): Try[StatusData] = Try {
      new StatusData(
        _id = model._id.map(mongo.ObjectID(_)) ?? mongo.ObjectID(),
        submitterId = model.submitterId,
        statusText = model.statusText,
        creationTime = model.creationTime ?? new js.Date()
      )
    }
  }

  /**
    * Status Data Extensions
    * @param data the given [[StatusData status data]]
    */
  implicit class StatusDataExtensions(val data: StatusData) extends AnyVal {

    def toModel = new Status(
      _id = data._id.map(_.toHexString()),
      submitterId = data.submitterId,
      statusText = data.statusText,
      creationTime = data.creationTime
    )
  }

  /**
    * Workload Extensions
    * @param model the given [[Workload status]]
    */
  implicit class WorkloadExtensions(val model: Workload) extends AnyVal {

    def toData(implicit mongo: MongoDB): Try[WorkloadData] = Try {
      new WorkloadData(
        _id = model._id.map(mongo.ObjectID(_)) ?? mongo.ObjectID(),
        name = model.name,
        technologyProduct = model.technologyProduct,
        azureServices = model.azureServices,
        customerSegment = model.customerSegment,
        businessSponsor = model.businessSponsor,
        technicalContact = model.technicalContact,
        msftLeadId = model.msftLeadId,
        msftLead = model.msftLead,
        statuses = model.statuses.map(_.flatMap(_.toData.toOption)),
        statusCode = model.statusCode,
        deployedStatus = model.deployedStatus,
        estimateGoLiveDate = model.estimateGoLiveDate,
        consumption = model.consumption,
        active = model.active,
        creationTime = model.creationTime ?? new js.Date(),
        lastUpdatedTime = model.lastUpdatedTime ?? new js.Date()
      )
    }
  }

  /**
    * Workload Data Extensions
    * @param data the given [[WorkloadData status data model]]
    */
  implicit class WorkloadDataExtensions(val data: WorkloadData) extends AnyVal {

    def toModel = new Workload(
      _id = data._id.map(_.toHexString()),
      name = data.name,
      technologyProduct = data.technologyProduct,
      azureServices = data.azureServices,
      customerSegment = data.customerSegment,
      businessSponsor = data.businessSponsor,
      technicalContact = data.technicalContact,
      msftLeadId = data.msftLeadId,
      msftLead = data.msftLead,
      statuses = data.statuses.map(_.map(_.toModel)),
      statusCode = data.statusCode,
      deployedStatus = data.deployedStatus,
      estimateGoLiveDate = data.estimateGoLiveDate,
      consumption = data.consumption,
      active = data.active,
      creationTime = data.creationTime ?? new js.Date(),
      lastUpdatedTime = data.lastUpdatedTime ?? new js.Date()
    )
  }

}