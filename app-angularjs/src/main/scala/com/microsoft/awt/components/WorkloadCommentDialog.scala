package com.microsoft.awt.components

import com.microsoft.awt.components.WorkloadCommentDialog._
import com.microsoft.awt.models.{User, Workload}
import org.scalajs.angularjs.AngularJsHelper._
import org.scalajs.angularjs._
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.angularjs.uibootstrap.{Modal, ModalInstance, ModalOptions}
import org.scalajs.dom.browser.console

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.{Failure, Success}

/**
  * Workload Comment Dialog
  * @author lawrence.daniels@gmail.com
  */
class WorkloadCommentDialog($modal: Modal) extends Service {

  def popup(workloadID: String) = {
    val $modalInstance = $modal.open[WorkloadCommentDialogResult](new ModalOptions(
      templateUrl = "workload_comment_dialog.html",
      controller = classOf[WorkloadCommentDialogController].getSimpleName,
      resolve = js.Dictionary("workloadID" -> ((() => workloadID): js.Function))
    ))
    $modalInstance.result
  }

}

/**
  * Workload Comment Dialog Companion
  * @author lawrence.daniels@gmail.com
  */
object WorkloadCommentDialog {

  type WorkloadCommentDialogResult = Workload

  /**
    * User Dialog Controller
    * @author lawrence.daniels@gmail.com
    */
  case class WorkloadCommentDialogController($scope: WorkloadCommentDialogScope, $modalInstance: ModalInstance[WorkloadCommentDialogResult],
                                             $timeout: Timeout, toaster: Toaster,
                                             @injected("workloadID") workloadID: String,
                                             @injected("MySession") mySession: MySessionFactory,
                                             @injected("UserFactory") userFactory: UserFactory,
                                             @injected("WorkloadService") workloadService: WorkloadService) extends Controller {

    $scope.form = {
      val form = new WorkloadCommentForm(workloadID)
      form.submitter = mySession.user
      form
    }
    $scope.processing = false

    $scope.init = () => {
      console.log(s"Initializing '${getClass.getSimpleName}'...")
      workloadService.getWorkloadByID(workloadID) onComplete {
        case Success(workload) =>
          $scope.$apply { () => $scope.workload = workload }
        case Failure(e) =>
          toaster.error("Loading Error", e.displayMessage)
      }
    }

    $scope.ok = (form: WorkloadCommentForm) => {
      $scope.processing = true
      workloadService.createStatus(form.workloadID, form.toStatus) onComplete {
        case Success(workload) =>
          $scope.$apply { () => $scope.processing = false }
          toaster.success("Workload Status", "Updated successfully")
          $modalInstance.close(workload)
        case Failure(e) =>
          $scope.$apply { () => $scope.processing = false }
          console.error(s"Failed to update user profile: ${e.displayMessage}")
          toaster.error("Workload Status", e.displayMessage)
      }
    }

    $scope.cancel = () => $modalInstance.dismiss("cancel")

  }

  /**
    * Workload Comment Dialog Scope
    * @author lawrence.daniels@gmail.com
    */
  @js.native
  trait WorkloadCommentDialogScope extends Scope {
    // properties
    var form: WorkloadCommentForm = js.native
    var processing: Boolean = js.native
    var workload: js.UndefOr[Workload] = js.native

    // functions
    var init: js.Function0[Unit] = js.native
    var cancel: js.Function0[Unit] = js.native
    var ok: js.Function1[WorkloadCommentForm, Unit] = js.native
  }

  /**
    * Workload Comment Form
    * @author lawrence.daniels@gmail.com
    */
  @ScalaJSDefined
  class WorkloadCommentForm(val workloadID: String) extends js.Object {
    var submitter: js.UndefOr[User] = js.undefined
    var statusText: js.UndefOr[String] = js.undefined
  }

  /**
    * Workload Comment Form Companion
    * @author lawrence.daniels@gmail.com
    */
  object WorkloadCommentForm {

    /**
      * Workload Comment Form Enrichment
      * @param form the given [[WorkloadCommentForm form]]
      */
    implicit class WorkloadCommentFormEnrichment(val form: WorkloadCommentForm) extends AnyVal {

      def toStatus: Workload.Status = new Workload.Status(
        submitterId = form.submitter.flatMap(_._id),
        statusText = form.statusText,
        creationTime = new js.Date()
      )
    }

  }

}