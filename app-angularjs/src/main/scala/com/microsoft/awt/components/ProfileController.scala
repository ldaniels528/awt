package com.microsoft.awt.components

import com.microsoft.awt.models.{Group, Post, User, Workload}
import com.microsoft.awt.ui.{Menu, MenuItem}
import org.scalajs.angularjs.AngularJsHelper._
import org.scalajs.angularjs._
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.dom.browser.{console, window}
import org.scalajs.sjs.JsUnderOrHelper._

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Profile Controller (AngularJS)
  * @author lawrence.daniels@gmail.com
  */
case class ProfileController($scope: ProfileControllerScope, $routeParams: ProfileRouteParams, $timeout: Timeout, toaster: Toaster,
                             @injected("GroupService") groupService: GroupService,
                             @injected("SessionFactory") sessionFactory: SessionFactory,
                             @injected("PostService") postService: PostService,
                             @injected("UserFactory") userFactory: UserFactory,
                             @injected("UserService") userService: UserService,
                             @injected("WorkloadService") workloadService: WorkloadService)
  extends Controller {

  // setup the navigation menu
  $scope.menus = js.Array(
    new Menu("MY PROFILE", items = js.Array(
      MenuItem.include(src = "/assets/views/home/navigation/profile_info.html")
    )),
    new Menu("ACTIONS", items = js.Array(
      MenuItem.include(src = "/assets/views/profiles/menu/actions.html")
    ))
  )

  ///////////////////////////////////////////////////////////////////////////
  //      Initialization Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.init = () => {
    console.log(s"Initializing '${getClass.getSimpleName}'...")

    // retrieve the user and its postings
    ensureProfileIsLoaded()
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Miscellaneous Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.isMe = () => {
    (for {
      id <- $scope.profileID.flat
      myId <- sessionFactory.user.flatMap(_._id).flat
    } yield id == myId).contains(true)
  }

  $scope.viewRecentActivity = () => {

  }

  ///////////////////////////////////////////////////////////////////////////
  //      Contact Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.contact = (aUser: js.UndefOr[User]) => {
    window.alert("Not yet implemented")
  }

  $scope.isContacted = (aUser: js.UndefOr[User]) => aUser map { user =>
    false
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Endorsement Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.endorse = (aUser: js.UndefOr[User]) => {
    for {
      endorsee <- aUser
      endorseeID <- endorsee._id
      endorser <- sessionFactory.user
      endorserID <- endorser._id
    } {
      $scope.endorseLoading = true
      userService.like(endorseeID, endorserID) onComplete {
        case Success(result) =>
          $timeout(() => $scope.endorseLoading = false, 500.millis)
          $scope.$apply { () =>
            if (result.success) {
              endorsee.endorsements = if (endorsee.endorsements.isEmpty) 1: Integer else endorsee.endorsements.map(_ + 1)
              endorsee.endorsers.foreach(_.push(endorserID))
            }
          }
        case Failure(e) =>
          $scope.endorseLoading = false
          console.error(e.displayMessage)
          toaster.error(e.displayMessage)
      }
    }
  }

  $scope.isEndorsed = (aUser: js.UndefOr[User]) => {
    for {
      user <- aUser
      endorser <- sessionFactory.user
      endorsers <- user.endorsers
      endorserId <- endorser._id
    } yield endorsers.contains(endorserId)
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Following Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.follow = (aUser: js.UndefOr[User]) => {
    for {
      followee <- aUser
      followeeID <- followee._id
      follower <- sessionFactory.user
      followerID <- follower._id
    } {
      $scope.followLoading = true
      userService.follow(followeeID, followerID) onComplete {
        case Success(result) =>
          $timeout(() => $scope.followLoading = false, 500.millis)
          $scope.$apply { () =>
            if (result.success) {
              followee.totalFollowers = if (followee.totalFollowers.isEmpty) 1: Integer else followee.totalFollowers.map(_ + 1)
              followee.followers.foreach(_.push(followerID))
            }
          }
        case Failure(e) =>
          $scope.followLoading = false
          console.error(e.displayMessage)
          toaster.error(e.displayMessage)
      }
    }
  }

  $scope.isFollowed = (aUser: js.UndefOr[User]) => {
    for {
      user <- aUser
      follower <- sessionFactory.user
      followers <- user.followers
      followerId <- follower._id
    } yield followers.contains(followerId)
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Report Issue Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.report = (aUser: js.UndefOr[User]) => {
    window.alert("Not yet implemented")
  }

  $scope.isReported = (aUser: js.UndefOr[User]) => aUser map { user =>
    false
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Private Functions
  ///////////////////////////////////////////////////////////////////////////

  private def ensureProfileIsLoaded() = {
    if ($scope.profileID.isEmpty) {
      $scope.profileID = $routeParams.id ?? sessionFactory.user.flatMap(_._id)
      $scope.profileID.foreach(loadGroupsPostingsAndWorkloads)
    }
  }

  /**
    * Retrieve the user instance and its groups and postings for the given user ID
    * @param userID the given user ID
    */
  private def loadGroupsPostingsAndWorkloads(userID: String) {
    console.log(s"Loading groups, postings and workloads for user # $userID...")
    $scope.loadingStart()
    val outcome = for {
      user <- userFactory.getUserByID(userID)
      groups <- groupService.getGroupsIncludingOrOwnedByUser(userID)
      posts <- postService.getPostsByUserID(userID)
      enrichedPosts <- userFactory.enrich(posts)
      workloads <- workloadService.getWorkloadsByUser(userID, $scope.activeOnly getOrElse true)
    } yield (user, groups, enrichedPosts, workloads)

    outcome onComplete {
      case Success((user, groups, posts, workloads)) =>
        $scope.$apply { () =>
          $scope.loadingDelayedStop(1.second)
          $scope.myGroups = groups
          $scope.profileUser = user
          $scope.posts = posts
          $scope.workloads = workloads
        }
      case Failure(e) =>
        $scope.$apply { () => $scope.loadingStop() }
        console.error(s"Loading Error: ${e.displayMessage}")
        toaster.error("Loading Error", "Failed while loading user information")
    }
  }

  private def refreshWorkloads(userId: String, activeOnly: Boolean) = {
    console.log(s"Loading workloads for user # $userId (activeOnly = $activeOnly)")
    workloadService.getWorkloadsByUser(userId, activeOnly) onComplete {
      case Success(workloads) =>
        $scope.$apply(() => $scope.workloads = workloads)
      case Failure(e) =>
        console.log(s"Loading Error: ${e.displayMessage}")
        toaster.error("Loading Error", "Failed while refreshing user workloads")
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Event Handlers
  ///////////////////////////////////////////////////////////////////////////

  $scope.onToggleActiveWorkloads((_, activeOnly) => $scope.profileID.flat foreach (refreshWorkloads(_, activeOnly)))

  $scope.onUserLoaded((_, user) => {
    console.log("User profile loaded...")
    ensureProfileIsLoaded()
  })

}

/**
  * Profile Controller Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait ProfileControllerScope extends Scope with GlobalLoadingScope {
  var menus: js.Array[Menu] = js.native
  var myGroups: js.UndefOr[js.Array[Group]] = js.native
  var profileID: js.UndefOr[String] = js.native
  var profileUser: js.UndefOr[User] = js.native
  var posts: js.Array[Post] = js.native
  var workloads: js.UndefOr[js.Array[Workload]] = js.native

  // init
  var init: js.Function0[Unit] = js.native

  // miscellaneous
  var isMe: js.Function0[Boolean] = js.native
  var activeOnly: js.UndefOr[Boolean] = js.native
  // inherited from WorkloadController
  var viewRecentActivity: js.Function0[Unit] = js.native

  // contact
  var contact: js.Function1[js.UndefOr[User], Unit] = js.native
  var contactLoading: js.UndefOr[Boolean] = js.native
  var isContacted: js.Function1[js.UndefOr[User], js.UndefOr[Boolean]] = js.native

  // endorsements
  var endorse: js.Function1[js.UndefOr[User], Unit] = js.native
  var endorseLoading: js.UndefOr[Boolean] = js.native
  var isEndorsed: js.Function1[js.UndefOr[User], js.UndefOr[Boolean]] = js.native

  // followers
  var follow: js.Function1[js.UndefOr[User], Unit] = js.native
  var followLoading: js.UndefOr[Boolean] = js.native
  var isFollowed: js.Function1[js.UndefOr[User], js.UndefOr[Boolean]] = js.native

  // report
  var report: js.Function1[js.UndefOr[User], Unit] = js.native
  var reportLoading: js.UndefOr[Boolean] = js.native
  var isReported: js.Function1[js.UndefOr[User], js.UndefOr[Boolean]] = js.native

}

/**
  * Profile Controller Route Parameters
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait ProfileRouteParams extends js.Object {
  var id: js.UndefOr[String] = js.native

}