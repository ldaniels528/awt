package com.microsoft.awt.javascript.services

import com.microsoft.awt.javascript.forms.{AccountActivationForm, VerificationResponse}
import com.microsoft.awt.javascript.models.Session
import org.scalajs.angularjs.Service
import org.scalajs.angularjs.http.Http

/**
  * Sign-Up Service (AngularJS)
  * @author lawrence.daniels@gmail.com
  */
class SignUpService($http: Http) extends Service {

  /**
    * Activates the user's account
    * @param form the given [[com.microsoft.awt.javascript.forms.AccountActivationForm form]]
    * @return a promise of the newly created [[Session response]]
    */
  def activateAccount(form: AccountActivationForm) = $http.post[Session]("/api/activate", form)

}
