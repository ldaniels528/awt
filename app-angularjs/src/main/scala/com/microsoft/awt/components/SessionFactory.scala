package com.microsoft.awt.components

import com.microsoft.awt.forms.LoginForm
import com.microsoft.awt.models.{AuthToken, Session, User, UserLike}
import org.scalajs.angularjs.AngularJsHelper._
import org.scalajs.angularjs._
import org.scalajs.angularjs.cookies.Cookies
import org.scalajs.angularjs.md5.MD5
import org.scalajs.angularjs.toaster._
import org.scalajs.dom.browser.console
import org.scalajs.sjs.JsUnderOrHelper._

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
  * Session Factory
  * @author lawrence.daniels@gmail.com
  */
class SessionFactory($rootScope: Scope, $cookies: Cookies, $location: Location, md5: MD5, $timeout: Timeout, toaster: Toaster,
                     @injected("AuthenticationService") authenticationService: AuthenticationService,
                     @injected("SessionService") sessionSvc: SessionService,
                     @injected("UserService") userSvc: UserService,
                     @injected("WebSocketService") webSocketSvc: WebSocketService) extends Factory {

  private val SESSION_COOKIE_NAME = "AWT_session_id"
  private val INITIAL_GRACE_PERIOD = 30000L

  var onlineStatuses = js.Dictionary[Session]()
  var mySession: js.UndefOr[Session] = js.undefined
  var myUser: js.UndefOr[User] = js.undefined
  val initTime = System.currentTimeMillis()

  ///////////////////////////////////////////////////////////////////////////
  //      Authorization Functions
  ///////////////////////////////////////////////////////////////////////////

  /**
    * Attempts to retrieve an authentication token from the server
    * @param username the username for whom the token is being requested
    * @return a new time-sensitive [[AuthToken authentication token]]
    */
  def getAuthToken(username: String) = authenticationService.getAuthToken(username)

  /**
    * Attempts to authenticate a user
    * @param tokenId   the given token ID
    * @param loginForm the given [[LoginForm login credentials]]
    * @return a promise of an authenticated [[Session session]]
    */
  def login(tokenId: String, loginForm: LoginForm) = authenticationService.login(tokenId, loginForm)

  /**
    * Signs the user belong to the token ID out of the system
    * @param tokenId the given token ID
    * @return the promise of a successful outcome
    */
  def logout(tokenId: String) = authenticationService.logout(tokenId)

  ///////////////////////////////////////////////////////////////////////////
  //      Public Functions
  ///////////////////////////////////////////////////////////////////////////

  /**
    * Retrieves the last updated time for the session for the given user
    * @param user the given [[com.microsoft.awt.models.UserLike user]]
    * @return the [[js.Date last updated time]] for the session
    */
  def getSessionLastUpdatedTime(user: UserLike) = {
    for {
      id <- user._id
      session <- onlineStatuses.get(id).orUndefined
      lastUpdated <- session.lastUpdated
    } yield lastUpdated
  }

  /**
    * Returns the current session
    * @return the current session
    */
  def session: js.UndefOr[Session] = {
    if (mySession.isDefined) mySession
    else {
      if (elapsedTime > INITIAL_GRACE_PERIOD) automaticSignInAsGuest()
      js.undefined
    }
  }

  /**
    * Returns the currently authentication user
    * @return the currently authentication user
    */
  def user: js.UndefOr[User] = {
    if (mySession.isDefined) myUser
    else {
      if (elapsedTime > INITIAL_GRACE_PERIOD) automaticSignInAsGuest()
      js.undefined
    }
  }

  /**
    * Loads the appropriate user for the given session
    * @param loadedSession the given [[com.microsoft.awt.models.Session session]]
    */
  def loadUserForSession(loadedSession: Session) {
    myUser = js.undefined
    announceSession(loadedSession)

    // is the session authorized? if so, load the user's profile
    session.flatMap(_.userID.flat).toOption match {
      case Some(userID) =>
        userSvc.getUserByID(userID) onComplete {
          case Success(loadedUser) => announceUser(loadedUser)
          case Failure(e) =>
            console.error(s"Failed to retrieve user: ${e.displayMessage}")
        }
      case None =>
        automaticSignInAsGuest()
    }
  }

  /**
    * Performs a system logout
    */
  def logout() {
    $cookies.remove(SESSION_COOKIE_NAME)
    mySession = js.undefined
    myUser = js.undefined
    automaticSignInAsGuest()
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Private Functions
  ///////////////////////////////////////////////////////////////////////////

  private def announceSession(loadedSession: Session) = {
    console.log(s"Session ${loadedSession._id} (${loadedSession.primaryEmail}) loaded in $elapsedTime msec")
    mySession = loadedSession
    setSessionCookie(loadedSession)
    $rootScope.emitSessionLoaded(loadedSession)
  }

  private def announceUser(loadedUser: User) = {
    console.log(s"User ${loadedUser.primaryEmail} loaded in $elapsedTime msec")
    myUser = loadedUser
    webSocketSvc.init(loadedUser)

    val userID = loadedUser._id getOrElse (throw new IllegalStateException("User ID not found"))
    updateOnlineStatusesForFollowers(userID)
    $rootScope.emitUserLoaded(loadedUser)
  }

  private def automaticSignInAsGuest() {
    val username = "guest"
    val outcome = for {
      token <- getAuthToken(username)
      hashPassword = md5.createHash(token.code + md5.createHash("123456"))
      session <- login(token.code, LoginForm(username, hashPassword))
    } yield session

    outcome onComplete {
      case Success(session) => loadUserForSession(session)
      case Failure(e) =>
        console.error(s"Failed to retrieve session for user '$username': ${e.displayMessage}")
    }
  }

  /**
    * Returns the elapsed time in milliseconds since the service started
    * @return the elapsed time in milliseconds
    */
  private def elapsedTime = System.currentTimeMillis() - initTime

  /**
    * Loads the session and user
    */
  private def loadSessionAndUser() {
    $cookies.get[String](SESSION_COOKIE_NAME).toOption match {
      case Some(sessionId) =>
        console.log(s"Attempting to load user session $sessionId...")
        val outcome = for {
          session <- sessionSvc.getSession(sessionId)
          userID = session.userID orDie "No user ID specified"
          user <- userSvc.getUserByID(userID)
        } yield (session, user)

        outcome onComplete {
          case Success((loadedSession, loadedUser)) =>
            announceSession(loadedSession)
            announceUser(loadedUser)
          case Failure(e) =>
            console.warn(s"Failed to retrieve session: ${e.displayMessage}")
            automaticSignInAsGuest()
        }
      case None =>
        automaticSignInAsGuest()
    }
  }

  private def updateOnlineStatusesForFollowers(userID: String): Unit = {
    for {
      thisSession <- mySession
      userID <- thisSession.userID
    } {
      console.log("Updating the online status for all followers...")
      val outcome = for {
        followers <- userSvc.getFollowers(userID)
        sessions <- sessionSvc.getSessions(followers.flatMap(_._id.toOption))
      } yield sessions

      outcome onComplete {
        case Success(sessions) =>
          onlineStatuses = js.Dictionary(sessions.map { session => (userID, session) }: _*)
          $timeout(() => updateOnlineStatusesForFollowers(userID), 5.minutes)
        case Failure(e) =>
          console.error(s"Failed to retrieve online statuses: ${e.displayMessage}")
      }
    }
  }

  private def setSessionCookie(loadedSession: Session) = {
    loadedSession._id.toOption match {
      case Some(sessionId) =>
        console.log(s"Setting cookie for session $sessionId")
        $cookies.put(SESSION_COOKIE_NAME, sessionId)
      case None =>
        console.warn(s"No session cookie was set - ${angular.toJson(loadedSession)}")
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Initialization
  ///////////////////////////////////////////////////////////////////////////

  // initialize by loading the session
  loadSessionAndUser()

}

