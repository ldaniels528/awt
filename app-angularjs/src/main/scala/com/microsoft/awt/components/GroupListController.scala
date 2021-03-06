package com.microsoft.awt.components

import com.microsoft.awt.models.{Group, User}
import org.scalajs.angularjs.AngularJsHelper._
import org.scalajs.angularjs._
import org.scalajs.angularjs.toaster._
import org.scalajs.dom.browser.console

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Group List Controller (AngularJS)
  * @author lawrence.daniels@gmail.com
  */
case class GroupListController($scope: GroupListScope, $location: Location, $timeout: Timeout, toaster: Toaster,
                               @injected("UserFactory") userFactory: UserFactory,
                               @injected("WorkloadService") workloadService: WorkloadService) extends Controller {


  ///////////////////////////////////////////////////////////////////////////
  //      Initialization Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.init = (label: js.UndefOr[String]) => {
    console.log(s"Initializing ${getClass.getSimpleName}... [$label]")

    // if the user has navigated to "My Groups" and there are no groups, redirect to "Other Groups"
    /*
    if ($location.path() == "/home/groups/mine" && $scope.groups.exists(_.nonEmpty)) {
      $location.path("/home/groups/others")
    }*/
    ()
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.getGroups = (theGroups: js.UndefOr[js.Array[Group]]) => theGroups map { groups =>
    enrichAllGroupsWithMembersAndWorkloads(groups)
    groups
  }

  $scope.toggleGroup = (aGroup: js.UndefOr[Group]) => aGroup foreach { group =>
    group.toggle()
    if (group.isExpanded && group.memberUsers.isEmpty) enrichGroupWithMembersAndWorkloads(group)
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Private Functions
  ///////////////////////////////////////////////////////////////////////////

  private def enrichAllGroupsWithMembersAndWorkloads(groups: js.Array[Group]) = {
    groups foreach { group =>
      if (group.memberUsers.isEmpty) enrichGroupWithMembersAndWorkloads(group)
    }
  }

  private def enrichGroupWithMembersAndWorkloads(group: Group) = group._id foreach { groupID =>
    console.log(s"Loading members and workloads for group '${group.name}'...")
    group.memberUsers = js.Array[User]()
    group.loading = true
    group.members foreach { ids =>
      val outcome = for {
        users <- userFactory.getUsers(ids)
        workloads <- workloadService.getWorkloadsByGroup(groupID)
      } yield (users, workloads)

      outcome onComplete {
        case Success((users, workloads)) =>
          $timeout(() => group.loading = false, 500.millis)
          $scope.$apply { () =>
            group.memberUsers = users
            group.workloads = workloads
          }
        case Failure(e) =>
          $timeout(() => group.loading = false, 250.millis)
          toaster.error("Loading Error", "Error loading group information")
          console.error(s"Error retrieving group members for ${group.name}: ${e.displayMessage}")
      }
    }
  }

}

/**
  * Group List Controller Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait GroupListScope extends Scope {
  // functions
  var init: js.Function1[js.UndefOr[String], Unit] = js.native
  var getGroups: js.Function1[js.UndefOr[js.Array[Group]], js.UndefOr[js.Array[Group]]] = js.native
  var toggleGroup: js.Function1[js.UndefOr[Group], Unit] = js.native

}