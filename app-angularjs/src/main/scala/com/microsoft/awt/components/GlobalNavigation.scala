package com.microsoft.awt.components

import org.scalajs.angularjs.{Controller, Location, Scope}
import org.scalajs.dom.browser.console

import scala.scalajs.js

/**
  * Global Navigation Feature
  * @author lawrence.daniels@gmail.com
  */
trait GlobalNavigation {
  self: Controller =>

  def $scope: GlobalNavigationScope

  def $location: Location

  ///////////////////////////////////////////////////////////////////////////
  //      Home Navigation Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.navigateToGroup = (aId: js.UndefOr[String]) => aId.foreach(id => navigateToPath(s"/home/groups/$id"))

  $scope.navigateToHome = () => navigateToPath("/home")

  $scope.navigateToLogin = () => navigateToPath("/login")

  $scope.navigateToMessages = () => navigateToPath("/home/messages")

  $scope.navigateToNewsFeed = () => navigateToPath("/home/newsfeed")

  $scope.navigateToPhotos = () => navigateToPath("/home/photos")

  $scope.navigateToProfile = (aId: js.UndefOr[String]) => aId.foreach(id => navigateToPath(s"/home/profile/$id"))

  ///////////////////////////////////////////////////////////////////////////
  //      Private Functions
  ///////////////////////////////////////////////////////////////////////////

  private def navigateToPath(url: String): Unit = {
    console.log(s"Navigating to '$url'...")
    $location.path(url)
  }

}

/**
  * Global Navigation Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait GlobalNavigationScope extends Scope {
  // home
  var navigateToGroup: js.Function1[js.UndefOr[String], Unit] = js.native
  var navigateToHome: js.Function0[Unit] = js.native
  var navigateToLogin: js.Function0[Unit] = js.native
  var navigateToMessages: js.Function0[Unit] = js.native
  var navigateToNewsFeed: js.Function0[Unit] = js.native
  var navigateToPhotos: js.Function0[Unit] = js.native
  var navigateToProfile: js.Function1[js.UndefOr[String], Unit] = js.native

}