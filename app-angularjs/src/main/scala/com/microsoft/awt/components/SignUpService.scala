package com.microsoft.awt.components

import com.microsoft.awt.forms.AccountActivationForm
import com.microsoft.awt.models.Session
import org.scalajs.angularjs.Service
import org.scalajs.angularjs.http.Http

/**
  * Sign-Up Service (AngularJS)
  * @author lawrence.daniels@gmail.com
  */
class SignUpService($http: Http) extends Service {

  /**
    * Activates the user's account
    * @param form the given [[com.microsoft.awt.forms.AccountActivationForm form]]
    * @return a promise of the newly created [[Session response]]
    */
  def activateAccount(form: AccountActivationForm) = $http.post[Session]("/api/activate", form)

}
