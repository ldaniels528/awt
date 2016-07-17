package com.microsoft.awt.javascript.controllers

import com.microsoft.awt.javascript.models.Attachment
import com.microsoft.awt.javascript.services.{MySessionService, PostService}
import org.scalajs.angularjs.AngularJsHelper._
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.angularjs.{Timeout, _}
import org.scalajs.dom.browser.console
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Photos Controller (AngularJS)
  * @author lawrence.daniels@gmail.com
  */
class PhotosController($scope: PhotosScope, $timeout: Timeout, toaster: Toaster,
                       @injected("MySession") mySession: MySessionService,
                       @injected("PostService") postService: PostService) extends Controller {

  $scope.photos = emptyArray

  ///////////////////////////////////////////////////////////////////////////
  //      Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.init = () => {
    console.log(s"Initializing '${getClass.getSimpleName}'...")
  }

  $scope.loadPhotos = () => {
    for {
      user <- mySession.user
      userID <- user._id
    } {
      console.log(s"Loading photos for user ${user.username} (${user._id})...")
      postService.getAttachmentsByUserID(userID) onComplete {
        case Success(photos) =>
          $scope.$apply(() => $scope.photos = photos)
        case Failure(e) =>
          console.error(s"Failed to retrieve photos: ${e.displayMessage}")
          toaster.error("Loading Error", "General fault while retrieving photos")
      }
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Event Listener Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.onUserLoaded((event, user) => {
    console.log(s"${getClass.getSimpleName}: user loaded - ${user.primaryEmail}")
    $scope.loadPhotos()
  })

}

@js.native
trait PhotosScope extends Scope {
  // properties
  var photos: js.Array[Attachment] = js.native

  // functions
  var init: js.Function0[Unit] = js.native
  var loadPhotos: js.Function0[Unit] = js.native

}