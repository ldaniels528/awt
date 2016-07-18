package com.microsoft.awt.javascript.components

import com.microsoft.awt.javascript.forms.AccountActivationForm
import org.scalajs.angularjs.AngularJsHelper._
import org.scalajs.angularjs._
import org.scalajs.angularjs.md5.MD5
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.dom.browser.console
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Sign-Up Controller (AngularJS)
  * @author lawrence.daniels@gmail.com
  */
class SignUpController($scope: SignUpControllerScope, $location: Location, md5: MD5, $timeout: Timeout, toaster: Toaster,
                       @injected("MySession") mySession: MySessionFactory,
                       @injected("SignUpService") signUpSvc: SignUpService)
  extends Controller {

  $scope.form = new AccountActivationForm()
  $scope.messages = emptyArray
  $scope.signupLoading = false

  ///////////////////////////////////////////////////////////////////////////
  //      Public Functions
  //////////////////////////////////////////////////////////////////////////

  $scope.init = () => {
    console.log(s"Initializing '${getClass.getSimpleName}'...")
  }

  $scope.signUp = (form: AccountActivationForm) => {
    if (validate(form)) {
      $scope.signupLoading = true
      val encPassword0 = md5.createHash(form.password0)
      val encPassword1 = md5.createHash(form.password1)
      signUpSvc.activateAccount(form.copy(password0 = encPassword0, password1 = encPassword1)) onComplete {
        case Success(session) =>
          console.log("Account activation successful... ")
          console.log(s"response = ${angular.toJson(session)}")
          $scope.$apply { () =>
            mySession.loadUserForSession(session)
            $scope.signupLoading = false
            $location.path("/home")
          }
        case Failure(e) =>
          console.log(e.displayMessage)
          $scope.$apply { () =>
            $scope.signupLoading = false
            $scope.messages.push(e.displayMessage)
          }
      }
    }
    ()
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Private Functions
  ///////////////////////////////////////////////////////////////////////////

  private def validate(form: AccountActivationForm) = {
    $scope.messages.removeAll()
    $scope.messages.push(form.validate(): _*)
    $scope.messages.isEmpty
  }

}

/**
  * Authentication Controller Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait SignUpControllerScope extends Scope {
  var form: AccountActivationForm = js.native
  var messages: js.Array[String] = js.native
  var signupLoading: Boolean = js.native

  // Functions
  var init: js.Function0[Unit] = js.native
  var signUp: js.Function1[AccountActivationForm, Unit] = js.native

}
