package com.microsoft.awt.javascript.components

import com.microsoft.awt.javascript.models.{User, Workload}
import org.scalajs.angularjs.AngularJsHelper._
import org.scalajs.angularjs._
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.dom.browser.{console, window}
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
  * Workload Controller
  * @author lawrence.daniels@gmail.com
  */
class WorkloadController($scope: WorkloadScope,
                         @injected("UserFactory") userFactory: UserFactory,
                         @injected("WorkloadDialog") workloadDialog: WorkloadDialog, toaster: Toaster,
                         @injected("WorkloadCommentDialog") workloadCommentDialog: WorkloadCommentDialog,
                         @injected("WorkloadService") workloadService: WorkloadService)
  extends Controller {

  $scope.activeOnly = true
  $scope.sortField = "name"
  $scope.sortAsc = true

  ///////////////////////////////////////////////////////////////////////////
  //      Initialization
  ///////////////////////////////////////////////////////////////////////////

  $scope.init = (aWorkloadVarName: js.UndefOr[String]) => {
    console.log(s"Initializing '${getClass.getSimpleName}'...")

    aWorkloadVarName foreach { name =>
      $scope.$watch(name, (theWorkloads: js.UndefOr[js.Array[Workload]]) => {
        console.log("Selecting default workload")
        selectDefaultWorkload(theWorkloads)
      })
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.addWorkload = (theWorkloads: js.UndefOr[js.Array[Workload]]) => theWorkloads foreach { workloads =>
    workloadDialog.popup().toFuture onComplete {
      case Success(newWorkload) =>
        $scope.$apply(() => workloads.push(newWorkload))
      case Failure(e) =>
        console.log(s"error: ${e.displayMessage}")
      // no toaster error here because workloadEditorDialog.cancel() generates an exception
    }
  }

  $scope.addComment = (aWorkload: js.UndefOr[Workload], theWorkloads: js.UndefOr[js.Array[Workload]]) => {
    for {
      workloads <- theWorkloads
      workload <- aWorkload
      workloadID <- workload._id
    } {
      workloadCommentDialog.popup(workloadID).toFuture onComplete {
        case Success(updatedWorkload) =>
          workloads.indexWhereOpt(_._id ?== updatedWorkload._id) foreach { index =>
            $scope.$apply { () =>
              workloads(index) = updatedWorkload
              $scope.selectWorkload(updatedWorkload)
            }
          }
        case Failure(e) =>
          console.log(s"error: ${e.displayMessage}")
        // no toaster error here because workloadEditorDialog.cancel() generates an exception
      }
    }
  }

  $scope.deactivateWorkload = (aWorkload: js.UndefOr[Workload], theWorkloads: js.UndefOr[js.Array[Workload]]) => {
    for {
      workloads <- theWorkloads
      workload <- aWorkload
      workloadID <- workload._id
    } {
      if (window.confirm("Are you sure you want to close this workload?")) {
        workloadService.deactivateWorkload(workloadID) onComplete {
          case Success(outcome) =>
            workloads.indexWhereOpt(_._id ?== workloadID) foreach { index =>
              $scope.$apply { () =>
                workloads.remove(index)
                selectDefaultWorkload(theWorkloads)
              }
            }
          case Failure(e) =>
            console.log(s"error: ${e.displayMessage}")
            toaster.error("Update Error", e.displayMessage)
        }
      }
    }
  }

  $scope.editWorkload = (aWorkload: js.UndefOr[Workload], theWorkloads: js.UndefOr[js.Array[Workload]]) => {
    for {
      workloads <- theWorkloads
      workload <- aWorkload
    } {
      workloadDialog.popup(workload).toFuture onComplete {
        case Success(updatedWorkload) =>
          workloads.indexWhereOpt(_._id ?== updatedWorkload._id) match {
            case Some(index) => $scope.$apply(() => workloads(index) = updatedWorkload)
            case None =>
              toaster.warning("Workload Update", "Workload updated but not found locally")
              workloads.push(updatedWorkload)
          }
          $scope.$apply(() => $scope.selectedWorkload = updatedWorkload)
        case Failure(e) =>
          console.log(s"error: ${e.displayMessage}")
        // no toaster error here because workloadEditorDialog.cancel() generates an exception
      }
    }
  }

  $scope.getServices = (aWorkload: js.UndefOr[Workload]) => for {
    workload <- aWorkload
    services <- workload.azureServices
  } yield services.mkString(", ")

  $scope.getStatusMembers = (aWorkload: js.UndefOr[Workload]) => aWorkload.flatMap(_.statusMembers)

  private def loadStatusMembers(workload: Workload) = {
    val userIds = workload.statuses.flatMap(_.flatMap(_.submitterId.toOption)).map(_.distinct) getOrElse emptyArray
    if (userIds.nonEmpty) {
      console.log(s"Loading status members for users # ${userIds.mkString(", ")}")
      userFactory.getUsers(userIds) onComplete {
        case Success(users) => $scope.$apply(() => workload.statusMembers = js.Array(users: _*))
        case Failure(e) =>
          console.log(s"Error retrieving statuses: ${e.displayMessage}")
          toaster.error("Loading Error", "Failed to retrieve workload status users")
      }
    }
  }

  $scope.getLatestStatusText = (aWorkload: js.UndefOr[Workload]) => aWorkload flatMap { workload =>
    getLatestStatus(workload).flatMap(_.statusText)
  }

  $scope.getLatestStatusTime = (aWorkload: js.UndefOr[Workload]) => aWorkload flatMap { workload =>
    getLatestStatus(workload).flatMap(_.creationTime)
  }

  def getLatestStatus(workload: Workload) = {
    workload.statuses.flatMap(_.sortBy(_.creationTime.map(_.toString).getOrElse("")).lastOption.orUndefined)
  }

  $scope.getWorkloadHighlightClass = (aWorkload: js.UndefOr[Workload], anIndex: js.UndefOr[Int]) => {
    for {
      status <- aWorkload.flat
      index <- anIndex.flat
    } yield {
      if ($scope.selectedWorkload ?== status) "status_highlighted"
      else if (index % 2 == 0) "status_even"
      else "status_odd"
    }
  }

  $scope.selectWorkload = (aWorkload: js.UndefOr[Workload]) => {
    $scope.selectedWorkload = aWorkload

    // load the status members
    aWorkload foreach { workload =>
      if (workload.statusMembers.isEmpty) loadStatusMembers(workload)
    }
  }

  $scope.sort = (theWorkloads: js.UndefOr[js.Array[Workload]]) => theWorkloads map { workloads =>
    val sortedWorkloads = $scope.sortField match {
      case "name" => workloads.toSeq.sortBy(_.name.getOrElse(""))
      case "msftLead" => workloads.toSeq.sortBy(_.msftLead.getOrElse(""))
      case "consumption" => workloads.toSeq.sortBy(_.consumption.getOrElse(0d))
      case "lastUpdatedTime" => workloads.toSeq.sortBy(_.lastUpdatedTime.map(_.toString()).getOrElse(""))
      case _ => workloads.toSeq
    }
    (if ($scope.sortAsc) sortedWorkloads else sortedWorkloads.reverse).toJSArray
  }

  $scope.toggleActiveWorkloads = () => {
    $scope.activeOnly = !$scope.activeOnly.contains(true)
    $scope.emitToggleActiveWorkloads($scope.activeOnly)
    ()
  }

  $scope.updateSorting = (field: String) => {
    if ($scope.sortField != field) {
      $scope.sortField = field
      $scope.sortAsc = true
    }
    else
      $scope.sortAsc = !$scope.sortAsc
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Private Functions
  ///////////////////////////////////////////////////////////////////////////

  private def selectDefaultWorkload(theWorkloads: js.UndefOr[js.Array[Workload]]) = {
    $scope.selectedWorkload = theWorkloads.flat.flatMap(_.headOption.orUndefined)
    $scope.selectWorkload($scope.selectedWorkload)
  }

}

/**
  * Workload Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait WorkloadScope extends Scope {
  // variables
  var activeOnly: js.UndefOr[Boolean] = js.native
  var selectedWorkload: js.UndefOr[Workload] = js.native

  // functions
  var init: js.Function1[js.UndefOr[String], Unit] = js.native
  var addWorkload: js.Function1[js.UndefOr[js.Array[Workload]], Unit] = js.native
  var addComment: js.Function2[js.UndefOr[Workload], js.UndefOr[js.Array[Workload]], Unit] = js.native
  var deactivateWorkload: js.Function2[js.UndefOr[Workload], js.UndefOr[js.Array[Workload]], Unit] = js.native
  var editWorkload: js.Function2[js.UndefOr[Workload], js.UndefOr[js.Array[Workload]], Unit] = js.native
  var getLatestStatusText: js.Function1[js.UndefOr[Workload], js.UndefOr[String]] = js.native
  var getLatestStatusTime: js.Function1[js.UndefOr[Workload], js.UndefOr[js.Date]] = js.native
  var getServices: js.Function1[js.UndefOr[Workload], js.UndefOr[String]] = js.native
  var getStatusMembers: js.Function1[js.UndefOr[Workload], js.UndefOr[js.Array[User]]] = js.native
  var getWorkloadHighlightClass: js.Function2[js.UndefOr[Workload], js.UndefOr[Int], js.UndefOr[String]] = js.native
  var selectWorkload: js.Function1[js.UndefOr[Workload], Unit] = js.native
  var toggleActiveWorkloads: js.Function0[Unit] = js.native

  // sorting
  var sortField: String = js.native
  var sortAsc: Boolean = js.native
  var sort: js.Function1[js.UndefOr[js.Array[Workload]], js.UndefOr[js.Array[Workload]]] = js.native
  var updateSorting: js.Function1[String, Unit] = js.native

}