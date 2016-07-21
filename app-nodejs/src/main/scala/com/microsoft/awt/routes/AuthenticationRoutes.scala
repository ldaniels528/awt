package com.microsoft.awt.routes

import com.microsoft.awt.data.CredentialDAO._
import com.microsoft.awt.data.SessionDAO._
import com.microsoft.awt.data.UserDAO._
import com.microsoft.awt.data._
import com.microsoft.awt.forms.AccountActivationForm
import com.microsoft.awt.models.{AuthToken, Session, User}
import org.scalajs.dom.browser.console
import org.scalajs.nodejs.NodeRequire
import org.scalajs.nodejs.express.{Application, Request, Response}
import org.scalajs.nodejs.mongodb._
import org.scalajs.nodejs.pvorb.md5.MD5
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Authentication Routes
  * @author lawrence.daniels@gmail.com
  */
object AuthenticationRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext, require: NodeRequire, mongo: MongoDB) = {
    implicit val credentialDAO = dbFuture.flatMap(_.getCredentialDAO)
    implicit val sessionDAO = dbFuture.flatMap(_.getSessionDAO)
    implicit val userDAO = dbFuture.flatMap(_.getUserDAO)
    implicit val md5 = MD5()

    app.post("/api/activate", (request: Request, response: Response, next: NextFunction) => activateAccount(request, response, next))
    app.delete("/api/auth/:tokenID/token", (request: Request, response: Response, next: NextFunction) => logout(request, response, next))
    app.get("/api/auth/:username/token", (request: Request, response: Response, next: NextFunction) => authToken(request, response, next))
    app.post("/api/auth/:tokenID/login", (request: Request, response: Response, next: NextFunction) => login(request, response, next))
  }

  def activateAccount(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, credentialDAO: Future[CredentialDAO], sessionDAO: Future[SessionDAO], userDAO: Future[UserDAO]) = {
    val form = request.bodyAs[AccountActivationForm]
    val messages = form.validate()
    if (messages.isEmpty) {
      val outcome = for {
        userOpt <- userDAO.flatMap(_.findAndUpdateByEmail(form.username.orNull, form.primaryEmail.orNull))
        user = userOpt getOrElse die(s"Account not found")
        writeResult <- credentialDAO.flatMap(_.insert(new CredentialData(username = user.username, md5Password = form.password0, creationTime = new js.Date())))
        _ = if (!writeResult.isOk) die(s"Account could not be activated")
        sessionOpt <- sessionDAO.flatMap(_.insert(toSession(user)) map {
          case result if result.isOk => result.opsAs[Session].find(_ != null)
          case result => die("The session could not be created")
        })
      } yield sessionOpt

      outcome onComplete {
        case Success(Some(session)) => response.send(session)
        case Success(_) => response.badRequest("Your account could not be activated")
        case Failure(e) => e.printStackTrace(); response.internalServerError(e); next()
      }
    }
    else {
      response.badRequest(doc("messages" -> messages))
      next()
    }
  }

  /**
    * Retrieves an authorization code, which is used during authentication
    * @example GET /api/auth/ldaniels/token
    */
  def authToken(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, credentialDAO: Future[CredentialDAO], sessionDAO: Future[SessionDAO], userDAO: Future[UserDAO]) = {
    val username = request.params("username")
    val outcome = for {
      userOpt <- userDAO.flatMap(_.findByUsername(username))
      newSession = userOpt map toSession getOrElse die("The username was invalid")
      result <- sessionDAO.flatMap(_.insert(newSession).toFuture)
    } yield result

    outcome onComplete {
      case Success(result) =>
        (for {
          session <- result.opsAs[SessionData].headOption
          id <- session._id.map(_.toHexString()).toOption
        } yield id) match {
          case Some(id) => response.send(new AuthToken(id, (System.currentTimeMillis() + 10000L).toDouble)); next()
          case None => response.badRequest("Session could not be created"); next()
        }
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
    * Authentication a user
    * @example POST /api/577ec3a7ff17dcf0a69dd0b2/login <= { "username":"ldaniels", "password":"$sadwqwe$%#" }
    */
  def login(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, md5: MD5, mongo: MongoDB, credentialDAO: Future[CredentialDAO], sessionDAO: Future[SessionDAO], userDAO: Future[UserDAO]) = {
    val sessionID = request.params("tokenID")

    // get the JSON body as a login form instance
    request.bodyAs[LoginForm] match {
      case form if form.username.nonAssigned => response.badRequest("The username is required"); next()
      case form if form.password.nonAssigned => response.badRequest("The password is required"); next()
      case form =>
        val (username, password) = (form.username.orNull, form.password.orNull)

        // lookup the user and its credentials
        val outcome = for {
          credentialOpt <- credentialDAO.flatMap(_.findByUsername(username))
          userOpt <- userDAO.flatMap(_.findByUsername(username))
          verified = verifyCredentials(sessionID, username, password, credentialOpt)
          sessionOpt <- if (verified) {
            console.log("Updating session # %s ...", sessionID)
            sessionDAO.flatMap(_.findAndUpdateByID(sessionID) map {
              case result if result.isOk => result.valueAs[Session]
              case result => console.error("The session could not be found"); None
            })
          }
          else Future.successful(None)
        } yield (verified, sessionOpt)

        outcome onComplete {
          case Success((verified, Some(session))) => response.send(session); next()
          case Success((verified, None)) =>
            val message = if (!verified) "Either the username or password is invalid" else "The session could not be found"
            response.badRequest(message)
            next()
          case Failure(e) =>
            response.internalServerError(e)
            next()
        }
    }
  }

  /**
    * De-Authorizes a user
    * @example DELETE /api/577ec3a7ff17dcf0a69dd0b2/token
    */
  def logout(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, sessionDAO: Future[SessionDAO]) = {
    val sessionID = request.params("tokenID")
    sessionDAO.flatMap(_.deleteOne(filter = "_id" $eq sessionID.$oid)) onComplete {
      case Success(outcome) => response.send(outcome.result)
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
    * Performs the actual authentication task
    * @param sessionID the session ID
    * @param username  the given user/screen name
    * @param password  the given password
    * @return a promise of an option of a Session
    */
  private def verifyCredentials(sessionID: String, username: String, password: String, credentialOpt: Option[CredentialData])(implicit ec: ExecutionContext, md5: MD5): Boolean = {
    (for {
      credential <- credentialOpt
    } yield {
      // verify the password
      val checkPassword = md5(sessionID + credential.md5Password)
      if (password == checkPassword) {
        console.log(s"User '$username' password matched")
        true
      }
      else {
        console.error(s"User '$username' password didn't match")
        false
      }
    }) getOrElse false
  }

  private def toSession(user: User) = new Session(
    userID = user._id,
    username = user.username,
    primaryEmail = user.primaryEmail,
    avatarURL = user.avatarURL,
    creationTime = new js.Date(),
    lastUpdated = js.Date.now(),
    isAnonymous = false
  )

  /**
    * Represents a Login Form
    */
  @js.native
  trait LoginForm extends js.Object {
    var username: js.UndefOr[String] = js.native
    var password: js.UndefOr[String] = js.native
  }

}
