package com.microsoft.awt.components

import com.microsoft.awt.components.GroupDialog.{GroupDialogController, GroupDialogResult}
import com.microsoft.awt.models.{Group, User}
import org.scalajs.angularjs.AngularJsHelper._
import org.scalajs.angularjs._
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.angularjs.uibootstrap.{Modal, ModalInstance, ModalOptions}
import org.scalajs.dom.browser.console
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.util.{Failure, Success}

/**
  * Group Dialog (Service)
  * @author lawrence.daniels@gmail.com
  */
class GroupDialog($modal: Modal) extends Service {

  def popup(group: js.UndefOr[Group] = js.undefined) = {
    val $modalInstance = $modal.open[GroupDialogResult](new ModalOptions(
      templateUrl = "group_dialog.html",
      controller = classOf[GroupDialogController].getSimpleName,
      resolve = js.Dictionary("updateGroup" -> ((() => group.flat): js.Function))
    ))
    $modalInstance.result
  }

}

/**
  * Group Dialog Companion
  * @author lawrence.daniels@gmail.com
  */
object GroupDialog {

  type GroupDialogResult = Group

  /**
    * Group Dialog Controller
    * @author lawrence.daniels@gmail.com
    */
  class GroupDialogController($scope: GroupDialogScope, $modalInstance: ModalInstance[GroupDialogResult],
                              $timeout: Timeout, toaster: Toaster,
                              @injected("updateGroup") updateGroup: js.UndefOr[Group],
                              @injected("MySession") mySession: MySessionFactory,
                              @injected("UserService") userService: UserService,
                              @injected("GroupService") groupService: GroupService) extends Controller {

    $scope.init = () => {
      console.log(s"Initializing '${getClass.getSimpleName}'...")
      userService.getUsers() onComplete {
        case Success(users) => $scope.$apply { () =>
          $scope.users = users
          $scope.form.owner = mySession.user.flatMap(_._id)
        }
        case Failure(e) =>
          toaster.error("Loading Error", e.displayMessage)
      }
    }

    $scope.ok = (aForm: js.UndefOr[GroupDialogForm]) => aForm.foreach { form =>
      $scope.processing = true
      save(form) onComplete {
        case Success(status) =>
          $timeout(() => $scope.processing = false, 500.millis)
          $modalInstance.close(status)
        case Failure(e) =>
          $timeout(() => $scope.processing = false, 500.millis)
          toaster.error("Workload Creation Error", e.displayMessage)
      }
    }

    $scope.cancel = () => $modalInstance.dismiss("cancel")

    private def save(form: GroupDialogForm): Future[Group] = {
      groupService.createGroup(form.toGroup)
    }

  }

  /**
    * Group Dialog Form
    * @author lawrence.daniels@gmail.com
    */
  @ScalaJSDefined
  class GroupDialogForm(var _id: js.UndefOr[String] = js.undefined,
                        var name: js.UndefOr[String] = js.undefined,
                        var description: js.UndefOr[String] = js.undefined,
                        var owner: js.UndefOr[String] = js.undefined,
                        var members: js.UndefOr[js.Array[String]] = js.undefined,
                        var creationTime: js.UndefOr[js.Date] = js.undefined) extends js.Object

  /**
    * Group Dialog Form Companion
    * @author lawrence.daniels@gmail.com
    */
  object GroupDialogForm {

    /**
      * Group Form Extensions
      * @param form the given [[GroupDialogForm form]]
      */
    implicit class GroupFormExtensions(val form: GroupDialogForm) extends AnyVal {

      def toGroup = new Group(
        _id = form._id,
        name = form.name,
        description = form.description,
        owner = form.owner,
        members = form.members,
        creationTime = new js.Date()
      )
    }

  }

  /**
    * Group Dialog Controller Scope
    * @author lawrence.daniels@gmail.com
    */
  @js.native
  trait GroupDialogScope extends Scope {
    // variables
    var form: GroupDialogForm = js.native
    var processing: Boolean = js.native
    var updating: Boolean = js.native
    var users: js.Array[User] = js.native

    // functions
    var init: js.Function0[Unit] = js.native
    var cancel: js.Function0[Unit] = js.native
    var ok: js.Function1[js.UndefOr[GroupDialogForm], Unit] = js.native

  }

}