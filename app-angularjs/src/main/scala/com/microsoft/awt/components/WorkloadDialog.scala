package com.microsoft.awt.components

import com.microsoft.awt.components.WorkloadDialog._
import com.microsoft.awt.models.{User, Workload}
import org.scalajs.angularjs.AngularJsHelper._
import org.scalajs.angularjs._
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.angularjs.uibootstrap._
import org.scalajs.dom.browser.console
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.{Failure, Success, Try}

/**
  * Workload Dialog (Service)
  * @author lawrence.daniels@gmail.com
  */
class WorkloadDialog($modal: Modal) extends Service {

  def popup(workload: js.UndefOr[Workload] = js.undefined) = {
    val $modalInstance = $modal.open[WorkloadDialogResult](new ModalOptions(
      templateUrl = "workload_dialog.html",
      controller = classOf[WorkloadDialogController].getSimpleName,
      resolve = js.Dictionary("updateWorkload" -> ((() => workload.flat): js.Function))
    ))
    $modalInstance.result
  }

}

/**
  * Workload Dialog Companion
  * @author lawrence.daniels@gmail.com
  */
object WorkloadDialog {

  type WorkloadDialogResult = Workload

  /**
    * Workload Dialog Controller
    * @author lawrence.daniels@gmail.com
    */
  class WorkloadDialogController($scope: WorkloadDialogScope, $modalInstance: ModalInstance[WorkloadDialogResult],
                                 $timeout: Timeout, toaster: Toaster,
                                 @injected("updateWorkload") updateWorkload: js.UndefOr[Workload],
                                 @injected("SessionFactory") sessionFactory: SessionFactory,
                                 @injected("UserService") userService: UserService,
                                 @injected("WorkloadService") statusService: WorkloadService) extends Controller {

    $scope.statusCodes = js.Array("GREEN", "YELLOW", "RED")
    $scope.updating = updateWorkload.nonEmpty
    $scope.form = updateWorkload.map(WorkloadDialogForm.apply) getOrElse new WorkloadDialogForm()
    $scope.processing = false
    $scope.users = emptyArray

    $scope.init = () => {
      console.log(s"Initializing '${getClass.getSimpleName}'...")
      userService.getUsers() onComplete {
        case Success(users) => $scope.$apply { () =>
          $scope.users = users
          $scope.form.msftLead = if ($scope.updating)
            users.find(_._id ?== updateWorkload.flatMap(_.msftLeadId)).orUndefined
          else
            users.find(_._id ?== sessionFactory.user.flatMap(_._id)).orUndefined
        }
        case Failure(e) =>
          toaster.error("Loading Error", e.displayMessage)
      }
    }

    $scope.ok = (aForm: js.UndefOr[WorkloadDialogForm]) => aForm.foreach { form =>
      $scope.processing = true
      save(form) onComplete {
        case Success(status) =>
          $timeout(() => $scope.processing = false, 500.millis)
          $modalInstance.close(status)
        case Failure(e) =>
          $timeout(() => $scope.processing = false, 500.millis)
          toaster.error("Workload Creation Error", e.displayMessage)
      }
    }

    $scope.cancel = () => $modalInstance.dismiss("cancel")

    private def save(form: WorkloadDialogForm): Future[Workload] = {
      val workload = form.toModel

      // set the common stuff first
      workload.lastUpdatedTime = new js.Date()
      workload.creationTime = if ($scope.updating) updateWorkload.flatMap(_.creationTime) ?? new js.Date() else new js.Date()
      workload.statuses = if ($scope.updating) updateWorkload.flatMap(_.statuses) ?? js.Array[Workload.Status]() else js.Array[Workload.Status]()
      if (form.statusText.flat.exists(_.trim.nonEmpty)) {
        workload.statuses.foreach(_.push(new Workload.Status(
          submitterId = sessionFactory.user.flatMap(_._id),
          statusText = form.statusText,
          creationTime = new js.Date()
        )))
      }

      // now do the create/update specific stuff
      if ($scope.updating) {
        workload._id = updateWorkload.flatMap(_._id)
        statusService.updateWorkload(workload) map {
          case result if result.nModified == 1 => workload
          case result =>
            console.error(s"update result = ${angular.toJson(result)}")
            throw new IllegalStateException("Workload status was not updated")
        }
      }
      else {
        statusService.createWorkload(workload)
      }
    }

  }

  /**
    * Workload Dialog Form
    * @author lawrence.daniels@gmail.com
    */
  @ScalaJSDefined
  class WorkloadDialogForm(var name: js.UndefOr[String] = js.undefined,
                           var technologyProduct: js.UndefOr[String] = js.undefined,
                           var azureServices: js.UndefOr[String] = js.undefined,
                           var customerSegment: js.UndefOr[String] = js.undefined,
                           var businessSponsor: js.UndefOr[String] = js.undefined,
                           var technicalContact: js.UndefOr[String] = js.undefined,
                           var msftLead: js.UndefOr[User] = js.undefined,
                           var statusCode: js.UndefOr[String] = "GREEN",
                           var statusText: js.UndefOr[String] = js.undefined,
                           var statuses: js.UndefOr[js.Array[Workload.Status]] = js.undefined,
                           var deployedStatus: js.UndefOr[String] = js.undefined,
                           var estimateGoLiveDate: js.UndefOr[String] = js.undefined,
                           var consumption: js.UndefOr[String] = js.undefined) extends js.Object {

    def toModel = {
      val now = new js.Date()
      new Workload(
        name = name,
        technologyProduct = technologyProduct,
        azureServices = azureServices.map(s => js.Array(s.split("[,]").map(_.trim).toSeq: _*)),
        customerSegment = customerSegment,
        businessSponsor = businessSponsor,
        technicalContact = technicalContact,
        msftLeadId = msftLead.flatMap(_._id),
        msftLead = msftLead.map(_.fullName),
        statusCode = statusCode,
        statuses = statuses,
        deployedStatus = deployedStatus,
        estimateGoLiveDate = estimateGoLiveDate.map(new js.Date(_)),
        consumption = consumption.flatMap(s => Try(s.trim.toDouble).toOption.orUndefined),
        active = true,
        creationTime = now,
        lastUpdatedTime = now
      )
    }
  }

  /**
    * Workload Dialog Form Companion
    * @author lawrence.daniels@gmail.com
    */
  object WorkloadDialogForm {

    def apply(workload: Workload) = {
      new WorkloadDialogForm(
        name = workload.name.flat,
        technologyProduct = workload.technologyProduct.flat,
        azureServices = workload.azureServices.flat.map(_.mkString(", ")),
        customerSegment = workload.customerSegment.flat,
        businessSponsor = workload.businessSponsor.flat,
        technicalContact = workload.technicalContact.flat,
        statusCode = workload.statusCode.flat,
        statuses = workload.statuses.flat,
        deployedStatus = workload.deployedStatus.flat,
        estimateGoLiveDate = workload.estimateGoLiveDate.flat.map(_.toDateString()),
        consumption = workload.consumption.flat.map(_.toString)
      )
    }

  }

  /**
    * Workload Dialog Scope
    * @author lawrence.daniels@gmail.com
    */
  @js.native
  trait WorkloadDialogScope extends Scope {
    // variables
    var form: WorkloadDialogForm = js.native
    var processing: Boolean = js.native
    var statusCodes: js.Array[String] = js.native
    var updating: Boolean = js.native
    var users: js.Array[User] = js.native

    // functions
    var init: js.Function0[Unit] = js.native
    var cancel: js.Function0[Unit] = js.native
    var ok: js.Function1[js.UndefOr[WorkloadDialogForm], Unit] = js.native

  }

}
