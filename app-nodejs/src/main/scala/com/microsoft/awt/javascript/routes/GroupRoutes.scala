package com.microsoft.awt.javascript.routes

import com.microsoft.awt.javascript.data.GroupDAO
import com.microsoft.awt.javascript.data.GroupDAO._
import com.microsoft.awt.javascript.data.GroupData._
import com.microsoft.awt.javascript.forms.MaxResultsForm
import com.microsoft.awt.javascript.models.Group
import org.scalajs.nodejs.express.{Application, Request, Response}
import org.scalajs.nodejs.mongodb._
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Group Routes
  * @author lawrence.daniels@gmail.com
  */
object GroupRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext, mongo: MongoDB) = {
    implicit val groupDAO = dbFuture.flatMap(_.getGroupDAO)

    // Group CRUD
    app.get("/api/group", (request: Request, response: Response, next: NextFunction) => getGroupByEntity(request, response, next))
    app.post("/api/group", (request: Request, response: Response, next: NextFunction) => createGroup(request, response, next))
    app.get("/api/group/:groupID", (request: Request, response: Response, next: NextFunction) => getGroupByID(request, response, next))

    app.get("/api/groups", (request: Request, response: Response, next: NextFunction) => getGroups(request, response, next))
    app.get("/api/groups/include/:userID", (request: Request, response: Response, next: NextFunction) => getInclusiveGroups(request, response, next))
    app.get("/api/groups/exclude/:userID", (request: Request, response: Response, next: NextFunction) => getExclusiveGroups(request, response, next))
  }

  /**
    * Creates a new group
    */
  def createGroup(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, groupDAO: Future[GroupDAO]) = {
    request.bodyAs[Group].toData match {
      case Success(group) =>
        groupDAO.flatMap(_.insert(group)) onComplete {
          case Success(result) if result.isOk => response.send(group); next()
          case Success(result) => response.badRequest(); next()
          case Failure(e) => response.internalServerError(e); next()
        }
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
    * Retrieve a group by ID
    */
  def getGroupByID(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, groupDAO: Future[GroupDAO]) = {
    val groupID = request.params("groupID")
    groupDAO.flatMap(_.findOneFuture[Group]("_id" $eq groupID.$oid)) onComplete {
      case Success(Some(group)) => response.send(group); next()
      case Success(None) => response.notFound(); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
    * Retrieve a group by entity (ID or name)
    */
  def getGroupByEntity(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, groupDAO: Future[GroupDAO]) = {
    val form = request.queryAs[GroupForm]
    val query = (form.name.map("name" $eq _: js.Any) ?? form.id.map("_id" $eq _.$oid)).toOption
    groupDAO map { coll =>
      query.map(coll.findOneFuture[Group](_)) match {
        case Some(task) =>
          task onComplete {
            case Success(Some(group)) => response.send(group)
            case Success(None) => response.notFound()
            case Failure(e) => response.internalServerError(e)
          }
        case None =>
          response.badRequest("Expected 'name' or 'id' parameters")
      }
    } onComplete {
      case Success(_) => next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
    * Retrieve all groups
    */
  def getGroups(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, groupDAO: Future[GroupDAO]) = {
    val maxResults = request.request.queryAs[MaxResultsForm].getMaxResults()
    groupDAO.flatMap(_.find().limit(maxResults).toArrayFuture[Group]) onComplete {
      case Success(groups) => response.send(groups); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
    * Retrieve groups that include a specific user
    */
  def getInclusiveGroups(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, groupDAO: Future[GroupDAO]) = {
    val userID = request.params("userID")
    val maxResults = request.request.queryAs[MaxResultsForm].getMaxResults()
    groupDAO.flatMap(_.find("members" $in js.Array(userID)).limit(maxResults).toArrayFuture[Group]) onComplete {
      case Success(groups) => response.send(groups); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
    * Retrieve groups that do not include a specific user
    */
  def getExclusiveGroups(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, groupDAO: Future[GroupDAO]) = {
    val userID = request.params("userID")
    val maxResults = request.request.queryAs[MaxResultsForm].getMaxResults()
    groupDAO.flatMap(_.find("members" $nin js.Array(userID)).limit(maxResults).toArrayFuture[Group]) onComplete {
      case Success(groups) => response.send(groups); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
    * Group Form
    * @author lawrence.daniels@gmail.com
    */
  @js.native
  trait GroupForm extends js.Object {
    var id: js.UndefOr[String] = js.native
    var name: js.UndefOr[String] = js.native
  }

}
