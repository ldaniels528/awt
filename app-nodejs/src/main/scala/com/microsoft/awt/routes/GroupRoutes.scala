package com.microsoft.awt.routes

import com.microsoft.awt.data.GroupDAO
import com.microsoft.awt.data.GroupDAO._
import com.microsoft.awt.data.GroupData._
import com.microsoft.awt.forms.MaxResultsForm
import com.microsoft.awt.models.Group
import org.scalajs.nodejs.express.{Application, Request, Response}
import org.scalajs.nodejs.mongodb._

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
    app.get("/api/group/:groupID", (request: Request, response: Response, next: NextFunction) => getGroupByID(request, response, next))
    app.post("/api/group", (request: Request, response: Response, next: NextFunction) => createGroup(request, response, next))
    app.put("/api/group/:groupID", (request: Request, response: Response, next: NextFunction) => updateGroup(request, response, next))

    // Group Collections
    app.get("/api/groups", (request: Request, response: Response, next: NextFunction) => getGroups(request, response, next))
    app.get("/api/groups/user/:userID/all", (request: Request, response: Response, next: NextFunction) => getGroupsIncludingOrOwnedByUser(request, response, next))
    app.get("/api/groups/user/:userID/in", (request: Request, response: Response, next: NextFunction) => getGroupsIncludingUser(request, response, next))
    app.get("/api/groups/user/:userID/nin", (request: Request, response: Response, next: NextFunction) => getGroupsExcludingUser(request, response, next))
  }

  /**
    * Creates a new group
    */
  def createGroup(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, groupDAO: Future[GroupDAO]) = {
    request.bodyAs[Group].toData match {
      case Success(group) =>
        groupDAO.flatMap(_.insert(group).toFuture) onComplete {
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
    * Retrieve groups that do not include a specific user
    */
  def getGroupsExcludingUser(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, groupDAO: Future[GroupDAO]) = {
    val userID = request.params("userID")
    val maxResults = request.request.queryAs[MaxResultsForm].getMaxResults()
    groupDAO.flatMap(_.find("members" $nin js.Array(userID)).limit(maxResults).toArrayFuture[Group]) onComplete {
      case Success(groups) => response.send(groups); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
    * Retrieve groups that include a specific user
    */
  def getGroupsIncludingUser(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, groupDAO: Future[GroupDAO]) = {
    val userID = request.params("userID")
    val maxResults = request.request.queryAs[MaxResultsForm].getMaxResults()
    groupDAO.flatMap(_.find("members" $in js.Array(userID)).limit(maxResults).toArrayFuture[Group]) onComplete {
      case Success(groups) => response.send(groups); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
    * Retrieves all groups that are owned by or include a specific user
    */
  def getGroupsIncludingOrOwnedByUser(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, groupDAO: Future[GroupDAO]) = {
    val userID = request.params("userID")
    val maxResults = request.request.queryAs[MaxResultsForm].getMaxResults()
    groupDAO.flatMap(_.find($or("owner" $eq userID, "members" $in js.Array(userID))).limit(maxResults).toArrayFuture[Group]) onComplete {
      case Success(groups) => response.send(groups); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
    * Updates an existing group by ID
    */
  def updateGroup(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, groupDAO: Future[GroupDAO]) = {
    val groupID = request.params("groupID")
    request.bodyAs[Group].toData match {
      case Success(group) =>
        groupDAO.flatMap(_.findOneAndUpdate(filter = "_id" $eq groupID.$oid, update = group).toFuture) onComplete {
          case Success(result) if result.isOk => response.send(group); next()
          case Success(result) => response.badRequest(); next()
          case Failure(e) => response.internalServerError(e); next()
        }
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
