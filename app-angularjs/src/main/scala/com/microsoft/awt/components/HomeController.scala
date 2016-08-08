package com.microsoft.awt.components

import com.microsoft.awt.models._
import com.microsoft.awt.ui.{Menu, MenuItem}
import org.scalajs.angularjs.AngularJsHelper._
import org.scalajs.angularjs._
import org.scalajs.angularjs.fileupload.nervgh.FileUploader
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.dom.browser.console
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Home Controller (AngularJS)
  * @author lawrence.daniels@gmail.com
  */
case class HomeController($scope: HomeControllerScope, $compile: js.Dynamic, $location: Location, $timeout: Timeout, toaster: Toaster,
                          @injected("EventService") eventService: EventService,
                          @injected("FileUploader") fileUploader: FileUploader,
                          @injected("SessionFactory") sessionFactory: SessionFactory,
                          @injected("PostService") postService: PostService,
                          @injected("UserFactory") userFactory: UserFactory,
                          @injected("UserDialog") userDialog: UserDialog,
                          @injected("UserService") userService: UserService)
  extends Controller with GlobalAuthorization with PostingCapabilities {

  // setup the navigation menu
  setupNavigationMenus()

  ///////////////////////////////////////////////////////////////////////////
  //      Initialization Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.showUpload = false

  $scope.init = () => {
    console.log(s"Initializing '${getClass.getSimpleName}'...")
    loadFollowersAndPostings()
    $scope.setupNewPost()
  }

  ///////////////////////////////////////////////////////////////////////////
  //      View Change Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.getActiveView = () => {
    $location.path() match {
      case "/home/events" =>
        "/assets/views/home/events/index.html"
      case "/home/groups/mine" =>
        "/assets/views/home/groups/mine.html"
      case "/home/groups/others" =>
        "/assets/views/home/groups/others.html"
      case s if s.startsWith("/home/groups/") =>
        "/assets/views/home/groups/details.html"
      case "/home/messages" =>
        "/assets/views/home/messages/index.html"
      case "/home/newsfeed" =>
        "/assets/views/home/posts/index.html"
      case "/home/photos" =>
        "/assets/views/home/photos/index.html"
      case "/home/profile" =>
        "/assets/views/home/profile/index.html"
      case s if s.startsWith("/home/profile/") =>
        "/assets/views/home/profile/index.html"
      case path =>
        console.warn(s"${getClass.getSimpleName}: Unrecognized path '$path'")
        "/assets/views/home/profile/index.html"
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Dialog Pop-up Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.profileEditorPopup = (aUserID: js.UndefOr[String]) => aUserID foreach { userID =>
    userDialog.popup(userID) onComplete {
      case Success(userForm) =>
        $scope.loadingStop()
        $scope.$apply(() => {
          sessionFactory.user foreach { myUser =>
            myUser.firstName = userForm.firstName
            myUser.lastName = userForm.lastName
            myUser.primaryEmail = userForm.primaryEmail
            myUser.title = userForm.title
            myUser.avatarURL = userForm.avatarURL
          }
        })
      case Failure(e) =>
        console.error(s"Profile edit error: ${e.displayMessage}")
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //      SEO / Web Summary Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.detectURL = (aPost: js.UndefOr[Post]) => aPost foreach { post =>
    if (!post.summaryLoaded.contains(true) && !post.summaryLoadQueued.contains(true)) {
      post.summaryLoadQueued = true
      $timeout(() => {
        console.log("Launching webpage summary loading process...")
        post.loading = true
        loadWebPageSummary(post) onComplete {
          case Success(_) =>
            $timeout(() => post.loading = false, 1.second)
            $scope.$apply(() => post.summaryLoadQueued = false)
          case Failure(e) =>
            $scope.$apply(() => post.loading = false)
            console.error(s"Metadata retrieval failed: ${e.displayMessage}")
        }
      }, 1.1.seconds)
    }
  }

  $scope.isRefreshable = (aPost: js.UndefOr[Post]) => {
    for {
      user <- sessionFactory.user
      post <- aPost
      text <- post.text
      submitterId <- post.submitterId
      userId <- user._id
    } yield text.contains("http") && (user.isAdmin.contains(true) || (submitterId == userId))
  }

  $scope.updateWebSummary = (aPost: js.UndefOr[Post]) => aPost foreach { post =>
    post.refreshLoading = true
    val outcome = for {
      summary <- loadWebPageSummary(post)
      updatedPost <- postService.updatePost(post)
    } yield updatedPost

    outcome onComplete {
      case Success(updatedPost) =>
        $timeout(() => post.refreshLoading = false, 1.second)
        $scope.$apply(() => $scope.updatePost(updatedPost))
      case Failure(e) =>
        $scope.$apply(() => post.refreshLoading = false)
        console.error(s"Metadata retrieval failed: ${e.displayMessage}")
    }
  }

  private def loadWebPageSummary(post: Post) = {
    val result = for {
      text <- post.text.flat.toOption.map(_.trim)
      lcText = text.toLowerCase
      start <- lcText.indexOfOpt("http://") ?? lcText.indexOfOpt("https://")
    } yield (text, start)

    result match {
      case Some((text, start)) =>
        // determine the span of the URL
        val limit = text.indexWhere(nonUrlCharacter, start)
        val end = if (limit != -1) limit else text.length
        val url = text.substring(start, end)
        console.log(s"webpage url => $url")

        // load the page summary information
        postService.getSharedContent(url) map { summary =>
          post.summary = summary
          post.tags = summary.tags
        }
      case None => Future.successful((): Unit)
    }
  }

  private def nonUrlCharacter(c: Char) = !(c.isLetterOrDigit || "_-+.:/?&=#%".contains(c))

  ///////////////////////////////////////////////////////////////////////////
  //      Upload Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.hideUploadPanel = () => $scope.showUpload = false

  $scope.isUploadVisible = () => $scope.showUpload

  $scope.toggleUploadPanel = () => $scope.showUpload = !$scope.showUpload

  ///////////////////////////////////////////////////////////////////////////
  //      Private Functions
  ///////////////////////////////////////////////////////////////////////////

  /**
    * Loads the user's followers and posts
    */
  private def loadFollowersAndPostings() = {
    for (userID <- sessionFactory.user.flatMap(_._id.flat)) {
      $scope.loadingStart()
      val outcome = for {
        posts <- postService.getNewsFeed(userID)
        enrichedPosts <- userFactory.enrich(posts)
      } yield enrichedPosts

      outcome onComplete {
        case Success(posts) =>
          $scope.loadingDelayedStop(1.second)
          $scope.$apply(() => $scope.posts = posts)
        case Failure(e) =>
          $scope.loadingDelayedStop(250.millis)
          toaster.error("Retrieval Error", "Error loading posts")
          console.error(s"Failed while retrieving posts: ${e.displayMessage}")
      }
    }
  }

  /**
    * Setups the navigation menu for either a registered user or a guest user
    * @param aUser the given optional [[User user]]
    */
  private def setupNavigationMenus(aUser: js.UndefOr[User] = js.undefined) {
    $scope.menus = js.Array(
      new Menu("MY PROFILE", items = js.Array(
        MenuItem(text = "Home", iconClass = "fa fa-home sk_home", action = { () => $scope.navigateToHome() }),
        MenuItem(text = "Edit Profile", iconClass = "fa fa-edit sk_profile_edit", action = { () => $scope.profileEditorPopup(sessionFactory.user.flatMap(_._id)) }),
        if (aUser.isEmpty || aUser.exists(_.username == "guest"))
          MenuItem(text = "Sign In", iconClass = "fa fa-sign-in sk_profile_edit", action = { () => $scope.navigateToLogin() })
        else
          MenuItem(text = "Sign Out", iconClass = "fa fa-sign-out sk_profile_edit", action = { () => $scope.logout() })
      )),
      new Menu("MY ACTIVITY", items = js.Array(
        MenuItem(text = "Newsfeed", iconClass = "fa fa-newspaper-o sk_news_feed", action = { () => $scope.navigateToNewsFeed() }),
        MenuItem(text = "Messages", iconClass = "fa fa-envelope-o sk_message", action = { () => $scope.navigateToMessages() }),
        MenuItem(text = "Photos", iconClass = "fa fa-file-image-o sk_photo", action = { () => $scope.navigateToPhotos() })
      )),
      new Menu("MY EVENTS", link = "#/home/events", items = js.Array(
        MenuItem.include(src = "/assets/views/home/navigation/my_events.html")
      )),
      new Menu("MY TEAMS", link = "#/home/groups/mine", items = js.Array(
        MenuItem.include(src = "/assets/views/home/navigation/my_groups.html")
      ))
    )
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Event Listener Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.onUserLoaded((_, user) => {
    console.log(s"${getClass.getSimpleName}: user loaded - ${user.primaryEmail}")
    $scope.init()
    setupNavigationMenus(user)
  })

  $scope.onWsPostMessage((_, post) => {
    console.log(s"Post received - ${angular.toJson(post)}")
    $scope.updatePost(post)
  })

}

/**
  * Home Controller Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait HomeControllerScope extends Scope with GlobalAuthorizationScope with GlobalLoadingScope with GlobalNavigationScope with PostingCapabilitiesScope {
  var menus: js.Array[Menu] = js.native
  var showUpload: Boolean = js.native
  var viewURL: String = js.native

  ///////////////////////////////////////////////////////////////////////////
  //      Public Functions
  ///////////////////////////////////////////////////////////////////////////

  // initialization
  var init: js.Function0[Unit] = js.native

  // dialogs
  var profileEditorPopup: js.Function1[js.UndefOr[String], Unit] = js.native

  // SEO/web summary
  var detectURL: js.Function1[js.UndefOr[Post], Unit] = js.native
  var isRefreshable: js.Function1[js.UndefOr[Post], js.UndefOr[Boolean]] = js.native
  var updateWebSummary: js.Function1[js.UndefOr[Post], Unit] = js.native

  // upload functions
  var isUploadVisible: js.Function0[Boolean] = js.native
  var hideUploadPanel: js.Function0[Unit] = js.native
  var toggleUploadPanel: js.Function0[Unit] = js.native

  // views
  var getActiveView: js.Function0[String] = js.native

}
