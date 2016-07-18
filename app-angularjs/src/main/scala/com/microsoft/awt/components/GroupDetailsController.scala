package com.microsoft.awt.components

import com.microsoft.awt.models.{Group, Workload}
import org.scalajs.angularjs.AngularJsHelper._
import org.scalajs.angularjs._
import org.scalajs.angularjs.toaster._
import org.scalajs.dom.browser.console
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.language.postfixOps
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
  * Group Details Controller
  * @author lawrence.daniels@gmail.com
  */
case class GroupDetailsController($scope: GroupDetailsScope, $routeParams: GroupDetailsRouteParams, $timeout: Timeout, toaster: Toaster,
                                  @injected("GroupService") groupService: GroupService,
                                  @injected("MySession") mySession: MySessionFactory,
                                  @injected("UserFactory") userFactory: UserFactory,
                                  @injected("WorkloadDialog") workloadDialog: WorkloadDialog,
                                  @injected("WorkloadCommentDialog") workloadCommentDialog: WorkloadCommentDialog,
                                  @injected("WorkloadService") workloadService: WorkloadService) extends Controller {

  ///////////////////////////////////////////////////////////////////////////
  //      Initialization
  ///////////////////////////////////////////////////////////////////////////

  $routeParams.groupId foreach { groupId =>
    $scope.currentUserId = mySession.user.flatMap(_._id)
    loadGroupWithMembersAndWorkloads(groupId)
  }

  $scope.init = () => {
    console.log(s"Initializing '${getClass.getSimpleName}'...")
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Private Functions
  ///////////////////////////////////////////////////////////////////////////

  private def loadGroupWithMembersAndWorkloads(groupId: String) = {
    console.log(s"Loading group '$groupId' with members and workloads...")
    val outcome = for {
      group <- groupService.getGroupByID(groupId).toFuture
      memberIds = group.members getOrElse emptyArray
      members <- userFactory.getUsers(memberIds)
      workloads <- workloadService.getWorkloadsByGroup(groupId).toFuture
    } yield (group, members, workloads)

    outcome onComplete {
      case Success((group, members, workloads)) =>
        $scope.$apply { () =>
          $scope.group = group
          group.memberUsers = members
          group.workloads = workloads

          // select the first workload
          $scope.selectedWorkload = workloads.headOption.orUndefined
        }
      case Failure(e) =>
        console.log(s"Failed to group #$groupId: ${e.displayMessage}")
        toaster.error("Loading Error", s"Failed to load group #$groupId")
    }
  }

}

/**
  * Group Details Controller Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait GroupDetailsScope extends Scope {
  // variables
  var currentUserId: js.UndefOr[String] = js.native
  var group: js.UndefOr[Group] = js.native
  var selectedWorkload: js.UndefOr[Workload] = js.native

  // functions
  var init: js.Function0[Unit] = js.native

}

/**
  * Group Details Route Parameters
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait GroupDetailsRouteParams extends js.Object {
  var groupId: js.UndefOr[String] = js.native
}