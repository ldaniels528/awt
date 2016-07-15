package com.microsoft.awt.javascript.dialogs

import com.microsoft.awt.javascript.dialogs.UserDialog.{UserDialogController, UserDialogResult}
import com.microsoft.awt.javascript.factories.UserFactory
import com.microsoft.awt.javascript.forms.ProfileEditForm
import com.microsoft.awt.javascript.services.MySessionService
import org.scalajs.angularjs.AngularJsHelper._
import org.scalajs.angularjs._
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.angularjs.uibootstrap.{Modal, ModalInstance, ModalOptions}
import org.scalajs.dom.browser.console

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * User Dialog
  * @author lawrence.daniels@gmail.com
  */
class UserDialog($modal: Modal) extends Service {

  def popup(userID: String) = {
    val $modalInstance = $modal.open[UserDialogResult](new ModalOptions(
      templateUrl = "user_dialog.html",
      controller = classOf[UserDialogController].getSimpleName,
      resolve = js.Dictionary("userID" -> ((() => userID): js.Function))
    ))
    $modalInstance.result
  }

}

/**
  * User Dialog Companion
  * @author lawrence.daniels@gmail.com
  */
object UserDialog {

  type UserDialogResult = ProfileEditForm

  /**
    * User Dialog Controller
    * @author lawrence.daniels@gmail.com
    */
  case class UserDialogController($scope: UserDialogScope, $modalInstance: ModalInstance[UserDialogResult],
                                  $timeout: Timeout, toaster: Toaster,
                                  @injected("userID") userID: String,
                                  @injected("MySession") mySession: MySessionService,
                                  @injected("UserFactory") userFactory: UserFactory) extends Controller {

    $scope.form = new ProfileEditForm()
    $scope.processing = false

    $scope.init = () => {
      console.log(s"Initializing '${getClass.getSimpleName}'...")
      userFactory.getUserByID(userID) onComplete {
        case Success(user) =>
          $scope.$apply { () => $scope.form = ProfileEditForm(user) }
        case Failure(e) =>
          toaster.error("Loading Error", e.displayMessage)
      }
    }

    $scope.ok = (form: ProfileEditForm) => {
      $scope.processing = true
      userFactory.updateUser(form) onComplete {
        case Success(user) =>
          $scope.$apply { () => $scope.processing = false }
          toaster.success("Profile Updated")
          $modalInstance.close(form)
        case Failure(e) =>
          $scope.$apply { () => $scope.processing = false }
          console.error(s"Failed to update user profile: ${e.displayMessage}")
          toaster.error("Profile Update Error", e.displayMessage)
      }
    }

    $scope.cancel = () => $modalInstance.dismiss("cancel")

  }

  /**
    * User Dialog Scope
    * @author lawrence.daniels@gmail.com
    */
  @js.native
  trait UserDialogScope extends Scope {
    // properties
    var form: ProfileEditForm = js.native
    var processing: Boolean = js.native

    // functions
    var init: js.Function0[Unit] = js.native
    var cancel: js.Function0[Unit] = js.native
    var ok: js.Function1[ProfileEditForm, Unit] = js.native
  }

}