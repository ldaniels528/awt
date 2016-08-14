package com.microsoft.awt.routes

import com.microsoft.awt.data.GroupDAO._
import com.microsoft.awt.data.WorkloadDAO._
import com.microsoft.awt.data.WorkloadData._
import com.microsoft.awt.data.{GroupDAO, WorkloadDAO, WorkloadData}
import com.microsoft.awt.models.{Group, Workload}
import org.scalajs.nodejs.NodeRequire
import org.scalajs.nodejs.express.{Application, Request, Response}
import org.scalajs.nodejs.mongodb._
import org.scalajs.nodejs.util.ScalaJsHelper._
import org.scalajs.sjs.JsUnderOrHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Workload Routes
  * @author lawrence.daniels@gmail.com
  */
object WorkloadRoutes {

  def init(app: Application, dbFuture: Future[Db])(implicit ec: ExecutionContext, mongo: MongoDB, require: NodeRequire) = {
    implicit val workloadDAO = dbFuture.flatMap(_.getWorkloadDAO)
    implicit val groupDAO = dbFuture.flatMap(_.getGroupDAO)

    // workload CRUD
    app.get("/api/workload/:workloadID", (request: Request, response: Response, next: NextFunction) => getWorkloadByID(request, response, next))
    app.post("/api/workload", (request: Request, response: Response, next: NextFunction) => createWorkload(request, response, next))
    app.put("/api/workload/:workloadID", (request: Request, response: Response, next: NextFunction) => updateWorkload(request, response, next))

    // workload state
    app.delete("/api/workload/:workloadID/active", (request: Request, response: Response, next: NextFunction) => deactivateWorkload(request, response, next))
    app.put("/api/workload/:workloadID/active", (request: Request, response: Response, next: NextFunction) => activateWorkload(request, response, next))

    // workload dataloads
    app.get("/api/workloads/download/:fileName", (request: Request, response: Response, next: NextFunction) => downloadWorkloads(request, response, next))
    app.get("/api/workloads/download/:groupID/group/:fileName", (request: Request, response: Response, next: NextFunction) => downloadWorkloadsByGroup(request, response, next))
    app.get("/api/workloads/download/:userID/user/:fileName", (request: Request, response: Response, next: NextFunction) => downloadWorkloadsByUser(request, response, next))

    // status CRUD
    app.delete("/api/workload/:workloadID/status/:statusID", (request: Request, response: Response, next: NextFunction) => deleteStatus(request, response, next))
    app.post("/api/workload/:workloadID/status", (request: Request, response: Response, next: NextFunction) => createStatus(request, response, next))

    // collection of workloads
    app.get("/api/workloads", (request: Request, response: Response, next: NextFunction) => getWorkloads(request, response, next))
    app.get("/api/workloads/group/:groupID", (request: Request, response: Response, next: NextFunction) => getWorkloadsByGroup(request, response, next))
    app.get("/api/workloads/user/:userID", (request: Request, response: Response, next: NextFunction) => getWorkloadsByUser(request, response, next))
  }

  /////////////////////////////////////////////////////////////////////////////////
  //      Status CRUD
  /////////////////////////////////////////////////////////////////////////////////

  /**
    * Creates a new status entry
    * @example POST /api/workload/5785a0da4da14095923df12d/status (<~ status in body)
    */
  def createStatus(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, workloadDAO: Future[WorkloadDAO]) = {
    val workloadID = request.params("workloadID")
    request.bodyAs[Workload.Status].toData match {
      case Success(status) =>
        workloadDAO.flatMap(_.findOneAndUpdate(filter = "_id" $eq workloadID.$oid, update = "statuses" $addToSet status,
          options = new FindAndUpdateOptions(returnOriginal = false))) onComplete {
          case Success(result) if result.isOk => response.send(result.value); next()
          case Success(result) => response.badRequest("Workload status could not be updated"); next()
          case Failure(e) => response.internalServerError(e); next()
        }
      case Failure(e) => response.badRequest(e); next()
    }
  }

  /**
    * Deletes a status entry
    * @example DELETE /api/workload/5785a0da4da14095923df12d/status/578774f1d21394f6a7952a57
    * @example {{{ db.workloads.update({_id:ObjectId("5785a0da4da14095923df12d")}, {$pull:{statuses:{_id: ObjectId("578774f1d21394f6a7952a57")} } }) }}}
    */
  def deleteStatus(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, workloadDAO: Future[WorkloadDAO]) = {
    val workloadID = request.params("workloadID")
    val statusID = request.params("statusID")
    workloadDAO.flatMap(_.findOneAndUpdate(filter = "_id" $eq workloadID.$oid, update = "statuses" $pull ("_id" $eq statusID.$oid))) onComplete {
      case Success(result) if result.isOk => response.send(result.value); next()
      case Success(result) => response.badRequest(s"Workload status $statusID could not be deleted"); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /////////////////////////////////////////////////////////////////////////////////
  //      Workload CRUD
  /////////////////////////////////////////////////////////////////////////////////

  /**
    * Creates a new workload
    * @example POST /api/workload [Workload in body]
    */
  def createWorkload(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, workloadDAO: Future[WorkloadDAO]) = {
    request.bodyAs[Workload].toData match {
      case Success(workload) =>
        workloadDAO.flatMap(_.insert(workload).toFuture) onComplete {
          case Success(result) if result.isOk => response.send(result.ops.headOption.orNull); next()
          case Success(result) => response.badRequest("Workload could not be created"); next()
          case Failure(e) => response.internalServerError(e); next()
        }
      case Failure(e) => response.badRequest(e); next()
    }
  }

  /**
    * Re-opens a deactivated (closed) workload
    * @example PUT /api/workload/5633c756d9d5baa77a7143a1/active
    */
  def activateWorkload(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, workloadDAO: Future[WorkloadDAO]) = {
    val workloadID = request.params("workloadID")
    workloadDAO.flatMap(_.updateOne(filter = doc("_id" -> workloadID.$oid), update = $set(doc("active" -> true, "lastUpdatedTime" -> new js.Date())))) onComplete {
      case Success(result) => response.send(result); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
    * Deactivates (closes) a workload
    * @example DELETE /api/workload/5633c756d9d5baa77a7143a1/active
    */
  def deactivateWorkload(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, workloadDAO: Future[WorkloadDAO]) = {
    val workloadID = request.params("workloadID")
    workloadDAO.flatMap(_.updateOne(filter = doc("_id" -> workloadID.$oid), update = $set(doc("active" -> false, "lastUpdatedTime" -> new js.Date())))) onComplete {
      case Success(result) => response.send(result); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
    * Downloads all workloads as CSV
    * @example GET /api/workloads/download/workloads.txt
    */
  def downloadWorkloads(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, workloadDAO: Future[WorkloadDAO]) = {
    workloadDAO.flatMap(_.find().toArrayFuture[Workload]) onComplete {
      case Success(workloads) => response.sendCSV(workloads); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
    * Downloads all workloads by group as CSV
    * @example GET /api/workloads/download/group/57885301a1750741df1f402f/workloads.txt
    */
  def downloadWorkloadsByGroup(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, groupDAO: Future[GroupDAO], workloadDAO: Future[WorkloadDAO]) = {
    val groupID = request.params("groupID")
    findWorkloadsByGroup(groupID, isActiveOnly(request)) onComplete {
      case Success(workloads) => response.sendCSV(workloads); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
    * Downloads all workloads by user as CSV
    * @example GET /api/workloads/download/user/57885301a1750741df1f402f/workloads.txt
    */
  def downloadWorkloadsByUser(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, groupDAO: Future[GroupDAO], workloadDAO: Future[WorkloadDAO]) = {
    val userID = request.params("userID")
    findWorkloadsByUser(userID, isActiveOnly(request)) onComplete {
      case Success(workloads) => response.sendCSV(workloads); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
    * Retrieve a workload by ID
    * @example GET /api/workload/5633c756d9d5baa77a7143a1
    */
  def getWorkloadByID(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, workloadDAO: Future[WorkloadDAO]) = {
    val workloadID = request.params("workloadID")
    workloadDAO.flatMap(_.findById[WorkloadData](workloadID)) onComplete {
      case Success(Some(workload)) => response.send(workload); next()
      case Success(None) => response.notFound(); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /////////////////////////////////////////////////////////////////////////////////
  //      Workload Collections
  /////////////////////////////////////////////////////////////////////////////////

  /**
    * Retrieve all workloads
    * @example GET /api/workloads
    */
  def getWorkloads(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, workloadDAO: Future[WorkloadDAO]) = {
    val query = if (isActiveOnly(request)) doc("active" $eq true) else doc()
    workloadDAO.flatMap(_.find(query).cursor.toArrayFuture[WorkloadData]) onComplete {
      case Success(workloads) => response.send(workloads); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
    * Retrieve the collection of workloads for a given group
    * @example GET /api/workloads/group/5633c756d9d5baa77a714803
    */
  def getWorkloadsByGroup(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, groupDAO: Future[GroupDAO], workloadDAO: Future[WorkloadDAO]) = {
    val groupID = request.params("groupID")
    findWorkloadsByGroup(groupID, isActiveOnly(request)) onComplete {
      case Success(workloads) => response.send(workloads); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
    * Retrieve the collection of workloads for a given user
    * @example GET /api/workloads/user/5633c756d9d5baa77a714803
    */
  def getWorkloadsByUser(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, groupDAO: Future[GroupDAO], workloadDAO: Future[WorkloadDAO]) = {
    val userID = request.params("userID")
    findWorkloadsByUser(userID, isActiveOnly(request)) onComplete {
      case Success(workloads) => response.send(workloads); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  /**
    * Update a workload by ID
    * @example PUT /api/workload/5633c756d9d5baa77a714803
    */
  def updateWorkload(request: Request, response: Response, next: NextFunction)(implicit ec: ExecutionContext, mongo: MongoDB, workloadDAO: Future[WorkloadDAO]) = {
    val workloadID = request.params("workloadID")
    request.bodyAs[Workload].toData match {
      case Success(workload) =>
        workloadDAO.flatMap(_.updateOne(filter = doc("_id" $eq workloadID.$oid), update = workload).toFuture) onComplete {
          case Success(result) => response.send(result); next()
          case Failure(e) => response.internalServerError(e); next()
        }
      case Failure(e) => response.badRequest(e); next()
    }
  }

  private def isActiveOnly(request: Request): Boolean = {
    request.queryAs[ActiveOnlyForm].activeOnly.flat.map(_.toLowerCase).contains("true")
  }

  private def findWorkloadsByGroup(groupID: String, activeOnly: Boolean)(implicit ec: ExecutionContext, mongo: MongoDB, groupDAO: Future[GroupDAO], workloadDAO: Future[WorkloadDAO]) = {
    for {
      groupOpt <- groupDAO.flatMap(_.findById[Group](groupID))
      group = groupOpt getOrElse die(s"Group #$groupID not found")
      memberIds = group.members getOrElse emptyArray
      query = if (activeOnly) doc("active" $eq true, "msftLeadId" $in memberIds) else doc("msftLeadId" $in memberIds)
      workloads <- workloadDAO.flatMap(_.find(query).toArrayFuture[Workload])
    } yield workloads
  }

  private def findWorkloadsByUser(userID: String, activeOnly: Boolean)(implicit ec: ExecutionContext, mongo: MongoDB, groupDAO: Future[GroupDAO], workloadDAO: Future[WorkloadDAO]) = {
    val query = if (activeOnly) doc("active" $eq true, "msftLeadId" $eq userID) else doc("msftLeadId" $eq userID)
    workloadDAO.flatMap(_.find(query).toArrayFuture[Workload])
  }

  /**
    * Active-Only Form
    * @author lawrence.daniels@gmail.com
    */
  @js.native
  trait ActiveOnlyForm extends js.Object {
    var activeOnly: js.UndefOr[String] = js.native
  }

}
