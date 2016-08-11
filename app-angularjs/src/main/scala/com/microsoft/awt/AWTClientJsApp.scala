package com.microsoft.awt

import com.microsoft.awt.components.GroupDialog.GroupDialogController
import com.microsoft.awt.components.UserDialog.UserDialogController
import com.microsoft.awt.components.WorkloadCommentDialog.WorkloadCommentDialogController
import com.microsoft.awt.components.WorkloadDialog.WorkloadDialogController
import com.microsoft.awt.components._
import com.microsoft.awt.directives._
import com.microsoft.awt.models.{User, Workload}
import org.scalajs.angularjs.Module._
import org.scalajs.angularjs._
import org.scalajs.angularjs.http.HttpProvider
import org.scalajs.angularjs.uirouter._
import org.scalajs.dom.browser.console

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

/**
  * Azure Workload Tracker (AWT) Client Application
  * @author lawrence.daniels@gmail.com
  */
@JSExport
object AWTClientJsApp extends js.JSApp {

  @JSExport
  override def main() {
    // create the application
    val module = angular.createModule("AWT", js.Array(
      "ngAnimate", "ngCookies", "ngRoute", "ngSanitize", "angularFileUpload", "angular-md5", "toaster", "ui.bootstrap"))

    // configure the Angular.js components
    configureControllers(module)
    configureDialogs(module)
    configureDirectives(module)
    configureFactories(module)
    configureFilters(module)
    configureServices(module)

    // configure the application (routes, etc.)
    configureApplication(module)

    // initialize the application
    runApplication(module)
  }

  private def configureControllers(module: Module): Unit = {
    module.controllerOf[AuthenticationController]("AuthenticationController")
    module.controllerOf[EventController]("EventController")
    module.controllerOf[GroupListController]("GroupListController")
    module.controllerOf[GroupDetailsController]("GroupDetailsController")
    module.controllerOf[HomeController]("HomeController")
    module.controllerOf[MainController]("MainController")
    module.controllerOf[PhotosController]("PhotosController")
    module.controllerOf[ProfileController]("ProfileController")
    module.controllerOf[SignUpController]("SignUpController")
    module.controllerOf[VerificationController]("VerificationController")
    module.controllerOf[WorkloadController]("WorkloadController")
  }

  private def configureDialogs(module: Module): Unit = {
    // group dialog
    module.serviceOf[GroupDialog]("GroupDialog")
    module.controllerOf[GroupDialogController]("GroupDialogController")

    // user dialog
    module.serviceOf[UserDialog]("UserDialog")
    module.controllerOf[UserDialogController]("UserDialogController")

    // workload dialog
    module.serviceOf[WorkloadDialog]("WorkloadDialog")
    module.controllerOf[WorkloadDialogController]("WorkloadDialogController")

    // workload comment dialog
    module.serviceOf[WorkloadCommentDialog]("WorkloadCommentDialog")
    module.controllerOf[WorkloadCommentDialogController]("WorkloadCommentDialogController")
  }

  private def configureDirectives(module: Module): Unit = {
    module.directiveOf[AvatarDirective]("avatar")
    module.directiveOf[NewsPostDirective]("awtPost")
    module.directiveOf[CensorableDirective]("censorable")
    module.directiveOf[CompileDirective]("compileA")
    module.directiveOf[FullNameDirective]("name")
    //module.directiveOf[NgThumbDirective]("ngThumb")
    module.directiveOf[RestrictedDirective]("restricted")
    module.directiveOf[WorkloadStatusDirective]("workloadStatus")
  }

  private def configureFactories(module: Module): Unit = {
    module.factoryOf[SessionFactory]("SessionFactory")
    module.factoryOf[UserFactory]("UserFactory")
  }

  private def configureFilters(module: Module): Unit = {
    module.filter("capitalize", Filters.capitalize)
    module.filter("duration", Filters.duration)
    module.filter("yesno", Filters.yesNo)
  }

  private def configureServices(module: Module): Unit = {
    module.serviceOf[AuthenticationService]("AuthenticationService")
    module.serviceOf[EventService]("EventService")
    module.serviceOf[GroupService]("GroupService")
    module.serviceOf[InboxMessageService]("InboxMessageService")
    module.serviceOf[NotificationService]("NotificationService")
    module.serviceOf[PostService]("PostService")
    module.serviceOf[ReactiveSearchService]("ReactiveSearchService")
    module.serviceOf[SessionService]("SessionService")
    module.serviceOf[SignUpService]("SignUpService")
    module.serviceOf[WorkloadService]("WorkloadService")
    module.serviceOf[UserService]("UserService")
    module.serviceOf[WebSocketService]("WebSocketService")
  }

  private def configureApplication(module: EnrichedModule): Unit = {
    module.config { ($httpProvider: HttpProvider, $routeProvider: RouteProvider) =>
      // enable cross domain calls
      $httpProvider.defaults("useXDomain") = true

      // remove the header used to identify AJAX call that would prevent CORS from working
      for {
        headers <- $httpProvider.defaults.get("headers").map(_.asInstanceOf[js.Dictionary[js.Any]])
        common <- headers.get("common").map(_.asInstanceOf[js.Dictionary[js.Any]])
      } common.remove("X-Requested-With")

      // configure the application routes
      $routeProvider
        .when("/home", RouteTo(redirectTo = "/home/profile"))
        .when("/home/events", RouteTo(templateUrl = "/assets/views/home/index.html"))
        .when("/home/groups/mine", RouteTo(templateUrl = "/assets/views/home/index.html"))
        .when("/home/groups/:groupId", RouteTo(templateUrl = "/assets/views/home/index.html"))
        .when("/home/messages", RouteTo(templateUrl = "/assets/views/home/index.html"))
        .when("/home/newsfeed", RouteTo(templateUrl = "/assets/views/home/index.html"))
        .when("/home/photos", RouteTo(templateUrl = "/assets/views/home/index.html"))
        .when("/home/profile", RouteTo(templateUrl = "/assets/views/home/index.html"))
        .when("/home/profile/:id", RouteTo(templateUrl = "/assets/views/home/index.html"))
        .when("/login", RouteTo(templateUrl = "/assets/views/login/index.html"))
        .when("/verification", RouteTo(templateUrl = "/assets/views/login/verification.html"))
        .otherwise(RouteTo(redirectTo = "/home/groups/mine"))
      ()
    }
  }

  private def runApplication(module: EnrichedModule): Unit = {
    module.run { ($rootScope: RootScope) =>
      console.log("Initializing AWT...")

      ///////////////////////////////////////////////////////////////////////////
      //      Global Functions
      ///////////////////////////////////////////////////////////////////////////

      $rootScope.getFullName = (aUser: js.UndefOr[User]) => aUser.map(_.fullName)
    }
  }

  /**
    * Application Root Scope
    * @author lawrence.daniels@gmail.com
    */
  @js.native
  trait RootScope extends Scope {
    // global functions
    var getFullName: js.Function1[js.UndefOr[User], js.UndefOr[String]] = js.native
    var getStatusClass: js.Function1[js.UndefOr[Workload], js.UndefOr[String]] = js.native
    var getStatusIcon: js.Function1[js.UndefOr[Workload], js.UndefOr[String]] = js.native
  }

}
