package com.microsoft.awt.routes

import com.microsoft.awt.data.SessionDAO
import com.microsoft.awt.data.SessionDAO._
import com.microsoft.awt.forms.MaxResultsForm
import com.microsoft.awt.models.Session
import org.scalajs.nodejs.express.{Application, Request, Response}
import org.scalajs.nodejs.mongodb._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Session Routes
  * @author lawrence.daniels@gmail.com
  */
object SessionRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext, mongo: MongoDB) = {
    implicit val sessionDAO = dbFuture.flatMap(_.getSessionDAO)

    // individual sessions
    app.get("/api/session/:sessionID", (request: Request, response: Response, next: NextFunction) => getSessionByID(request, response, next))

    // collections
    app.get("/api/sessions", (request: Request, response: Response, next: NextFunction) => getSessions(request, response, next))
  }

  /**
    * Retrieve a session by ID
    */
  def getSessionByID(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, sessionDAO: Future[SessionDAO]) = {
    val sessionID = request.params("sessionID")
    sessionDAO.flatMap(_.findById[Session](sessionID)) onComplete {
      case Success(Some(session)) => response.send(session); next()
      case Success(None) => response.notFound(); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
    * Retrieve a sessions by IDs
    */
  def getSessions(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, sessionDAO: Future[SessionDAO]) = {
    val form = request.queryAs[UserIdListForm]
    form.userIDs.toOption match {
      case Some(userIDs) =>
        val ids = userIDs.map(_.$oid)
        sessionDAO.flatMap(_.find("_id" $in js.Array(ids)).limit(form.getMaxResults()).toArrayFuture[Session]) onComplete {
          case Success(sessions) => response.send(sessions); next()
          case Failure(e) => response.internalServerError(e); next()
        }
      case None =>
        response.send(js.Array()); next()
    }
  }

  /**
    * User ID List Form
    * @author lawrence.daniels@gmail.com
    */
  @js.native
  trait UserIdListForm extends MaxResultsForm {
    var userIDs: js.UndefOr[js.Array[String]] = js.native
  }

}
