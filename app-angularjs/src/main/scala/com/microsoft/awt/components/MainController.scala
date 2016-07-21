package com.microsoft.awt.components

import com.microsoft.awt.models._
import com.microsoft.awt.ui.MainTab
import org.scalajs.angularjs.AngularJsHelper._
import org.scalajs.angularjs._
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.dom.browser.console
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.annotation.tailrec
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Main Controller (AngularJS)
  * @author lawrence.daniels@gmail.com
  */
case class MainController($scope: MainScope, $location: Location, $q: Q, $timeout: Timeout, toaster: Toaster,
                          @injected("AuthenticationService") authenticationService: AuthenticationService,
                          @injected("EventService") eventService: EventService,
                          @injected("GroupService") groupService: GroupService,
                          @injected("MySession") mySession: MySessionFactory,
                          @injected("NotificationService") notificationSvc: NotificationService,
                          @injected("ReactiveSearchService") reactiveSearchSvc: ReactiveSearchService,
                          @injected("WebSocketService") webSocketSvc: WebSocketService)
  extends AutoCompletionController($scope, $q, reactiveSearchSvc) with GlobalLoading with GlobalNavigation {

  $scope.tabs = js.Array(
    //MainTab(name = "Home", url = "/home", icon = "fa fa-home")
  )

  var connected = false
  $scope.notifications = emptyArray

  ///////////////////////////////////////////////////////////////////////////
  //      Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.init = () => {
    console.log(s"Initializing '${getClass.getSimpleName}'...")
  }

  $scope.formatSearchResult = (aResult: js.UndefOr[EntitySearchResult]) => for {
    result <- aResult
    name <- result.name
  } yield name

  /**
    * Returns the appropriate image URL for the given user
    */
  $scope.getAvatarURL = (aUser: js.UndefOr[User]) => aUser.flatMap { user =>
    // TODO need to get the real user's image when only the userID is present
    if (user.avatarURL.exists(_.nonEmpty))
      user.avatarURL
    else
      "/assets/images/avatars/anonymous.png"
  }

  $scope.getOnlineStatus = (aUser: js.UndefOr[UserLike]) => {
    for {
      user <- aUser
      lastUpdated <- mySession.getSessionLastUpdatedTime(user)
    } yield {
      if (System.currentTimeMillis().toDouble - lastUpdated < 15) "GREEN" else "YELLOW"
    }
  }

  $scope.getUpcomingEvents = () => $scope.events

  $scope.isConnected = () => connected

  $scope.isLoginPage = () => $location.path() == "/login"

  $scope.isSelectedTab = (aTab: js.UndefOr[MainTab]) => aTab map (tab => $location.path().startsWith(tab.url))

  $scope.logout = () => mySession.session.flatMap(_._id) foreach { sessionID =>
    $scope.loadingStart()
    authenticationService.logout(sessionID) onComplete {
      case Success(result) =>
        if (result.isOk) mySession.logout() else toaster.error("Logout Failure", "")
        $scope.$apply { () => $scope.loadingStop() }
      case Failure(e) =>
        toaster.error("Logout Failure", e.displayMessage)
        $scope.$apply { () => $scope.loadingStop() }
    }
  }

  $scope.onSelectedItem = (item: js.UndefOr[js.Any], aModel: js.UndefOr[EntitySearchResult], label: js.UndefOr[String]) => {
    for {
      model <- aModel
      entity <- model.`type`
      modelId <- model._id
    } {
      console.log(s"Handling $entity $label")
      entity match {
        case "GROUP" =>
          $scope.navigateToGroup(modelId)
        case "USER" =>
          $scope.navigateToProfile(modelId)
        case _ =>
          console.warn(s"Entity type '$entity' was unhandled")
      }
    }
  }

  $scope.setActiveTab = (tab: MainTab) => $location.path(tab.url)

  $scope.session = () => mySession.session

  $scope.showFullName = (aUser: js.UndefOr[User]) => aUser.flatMap(_.fullName)

  $scope.toggled = (open: js.UndefOr[Boolean]) => open foreach { isOpen =>
    console.log(s"toggled open ? $isOpen")
  }

  $scope.user = () => mySession.user

  ///////////////////////////////////////////////////////////////////////////
  //      Notification Functions
  ///////////////////////////////////////////////////////////////////////////

  /**
    * Deletes the given notification
    */
  $scope.deleteNotification = (aNotification: js.UndefOr[Notification]) => {
    for {
      notification <- aNotification
      id <- notification._id
    } {
      console.log(s"Deleting notification $id...")
      notificationSvc.deleteNotification(id) onComplete {
        case Success(result) =>
          console.log(s"result => ${angular.toJson(result)}")
          if (result.success) {
            $scope.notifications.indexWhere(_._id ?== notification._id) match {
              case -1 => toaster.error("Failed to delete notification")
              case index => $scope.notifications.remove(index)
            }
          }
        case Failure(e) =>
          console.error(s"Failed to delete notification: ${e.displayMessage}")
          toaster.error("Failed to delete notification")
      }
    }
  }

  $scope.getNotificationGlyphClass = (aNotification: js.UndefOr[Notification]) => {
    for {
      notification <- aNotification
      kind <- notification.`type`
    } yield getNotificationTypeIcon(kind)
  }

  $scope.getNotifications = () => $scope.notifications

  $scope.getNotificationsByType = (aNotificationType: js.UndefOr[String]) => {
    for {
      notificationType <- aNotificationType
    } yield $scope.notifications.filter(_.`type`.exists(_ == notificationType))
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Private Functions
  ///////////////////////////////////////////////////////////////////////////

  /**
    * Returns the appropriate icon for the given event type
    * @param eventType the given event type (e.g. "ACHIEVEMENT")
    * @return the icon CSS styles
    */
  @tailrec
  private def getNotificationTypeIcon(eventType: String): String = eventType match {
    case "ACHIEVEMENT" => "fa-diamond sk_diamond"
    case "COMMENT" => "fa-comment-o sk_comment"
    case "EVENT" => "fa-calendar sk_calender"
    case "LEVELUP" => "fa-tachometer sk_levelup"
    case "NOTIFICATION" => "fa-info-circle sk_notifications"
    case "TEST" => "fa-book sk_test"
    case _ =>
      console.warn(s"Unrecognized event type '$eventType'")
      getNotificationTypeIcon("NOTIFICATION")
  }

  private def loadEventsAndNotifications(session: Session) = session.userID foreach { userID =>
    console.log("Loading events, groups and notifications...")
    val outcome = for {
      events <- eventService.getEvents(userID)
      groups <- groupService.getGroupsOwnedByOrIncludeUser(userID, 10)
      notifications <- notificationSvc.getNotificationsByUserID(userID, unread = true)
    } yield (events, groups, notifications)

    outcome onComplete {
      case Success((events, groups, notifications)) =>
        $scope.events = events
        $scope.groups = groups
        $scope.notifications = notifications
      case Failure(e) =>
        console.error(s"Failed while retrieving events, groups and notifications: ${e.displayMessage}")
        toaster.error("Loading Error", "Failed to load events and notifications")
        e.printStackTrace()
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Event Listener Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.onSessionLoaded((_, session) => loadEventsAndNotifications(session))

  $scope.onWsNotificationMessage((_, notification) => {
    console.log(s"Received notification: ${angular.toJson(notification, pretty = true)}")
    $scope.notifications.push(notification)
  })

  $scope.onWsStateChange((_, connected) => {
    console.log(s"Web socket state change: connected = $connected")
    this.connected = connected
  })

}

/**
  * Main Controller Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait MainScope extends Scope with AutoCompletionScope with GlobalLoadingScope with GlobalNavigationScope {
  var events: js.UndefOr[js.Array[Event]] = js.native
  var groups: js.UndefOr[js.Array[Group]] = js.native
  var notifications: js.Array[Notification] = js.native
  var searchTerm: js.UndefOr[String] = js.native
  var tabs: js.Array[MainTab] = js.native

  ///////////////////////////////////////////////////////////////////////////
  //      Public Functions
  ///////////////////////////////////////////////////////////////////////////

  var init: js.Function0[Unit] = js.native
  var formatSearchResult: js.Function1[js.UndefOr[EntitySearchResult], js.UndefOr[String]] = js.native
  var deleteNotification: js.Function1[js.UndefOr[Notification], Unit] = js.native
  var getAvatarURL: js.Function1[js.UndefOr[User], js.UndefOr[String]] = js.native
  var getNotificationGlyphClass: js.Function1[js.UndefOr[Notification], js.UndefOr[String]] = js.native
  var getNotifications: js.Function0[js.Array[Notification]] = js.native
  var getNotificationsByType: js.Function1[js.UndefOr[String], js.UndefOr[js.Array[Notification]]] = js.native
  var getUpcomingEvents: js.Function0[js.UndefOr[js.Array[Event]]] = js.native
  var isConnected: js.Function0[Boolean] = js.native
  var isLoginPage: js.Function0[Boolean] = js.native
  var getOnlineStatus: js.Function1[js.UndefOr[UserLike], js.UndefOr[String]] = js.native
  var isSelectedTab: js.Function1[js.UndefOr[MainTab], js.UndefOr[Boolean]] = js.native
  var logout: js.Function0[Unit] = js.native
  var onSelectedItem: js.Function3[js.UndefOr[js.Any], js.UndefOr[EntitySearchResult], js.UndefOr[String], Unit] = js.native
  var session: js.Function0[js.UndefOr[Session]] = js.native
  var setActiveTab: js.Function1[MainTab, Any] = js.native
  var showFullName: js.Function1[js.UndefOr[User], js.UndefOr[String]] = js.native
  var toggled: js.Function1[js.UndefOr[Boolean], Unit] = js.native
  var user: js.Function0[js.UndefOr[User]] = js.native

}

