package com.microsoft.awt.javascript.controllers

import com.microsoft.awt.javascript.forms.LoginForm
import com.microsoft.awt.javascript.services.{AuthenticationService, MySessionService}
import org.scalajs.angularjs.AngularJsHelper._
import org.scalajs.angularjs.md5.MD5
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.angularjs.{Controller, Location, Scope, injected}
import org.scalajs.dom.browser.console
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Authentication Controller (AngularJS)
  * @author lawrence.daniels@gmail.com
  */
class AuthenticationController($scope: AuthenticationControllerScope, $location: Location, md5: MD5, toaster: Toaster,
                               @injected("AuthenticationService") authenticationService: AuthenticationService,
                               @injected("MySession") mySession: MySessionService)
  extends Controller {

  $scope.form = LoginForm()
  $scope.loginLoading = false
  $scope.messages = emptyArray

  ///////////////////////////////////////////////////////////////////////////
  //      Initialization Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.init = () => {
    console.log(s"Initializing '${getClass.getSimpleName}'...")
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.login = (aForm: js.UndefOr[LoginForm]) => aForm foreach { form =>
    $scope.messages = form.validate
    if ($scope.messages.isEmpty) {
      $scope.loginLoading = true

      // requests an authentication token, then uses it to authorize the user
      val outcome = for {
        authData <- authenticationService.getAuthToken(form.username.orNull).toFuture
        hashPassword = md5.createHash(authData.code + md5.createHash(form.password))
        session <- authenticationService.login(authData.code, new LoginForm(form.username, hashPassword)).toFuture
      } yield (authData, session)

      outcome onComplete {
        case Success((authData, session)) =>
          console.log("Login successful... ")
          $scope.$apply(() => {
            mySession.loadUserForSession(session)
            $scope.loginLoading = false
            $location.path("/home")
          })
        case Failure(e) =>
          toaster.error("Authentication Failure", e.displayMessage)
          $scope.$apply(() => {
            $scope.loginLoading = false
            $scope.messages.push(e.displayMessage)
            console.log(e.displayMessage)
          })
      }
    }
  }

}

/**
  * Authentication Controller Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait AuthenticationControllerScope extends Scope {
  var messages: js.Array[String] = js.native
  var loginLoading: js.UndefOr[Boolean] = js.native
  var form: LoginForm = js.native

  // Functions
  var init: js.Function0[Unit] = js.native
  var login: js.Function1[js.UndefOr[LoginForm], Unit] = js.native

}

