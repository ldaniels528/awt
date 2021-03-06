package com.microsoft.awt

import com.microsoft.awt.models.{Workload, WorkloadLike}
import org.scalajs.nodejs.express.Response
import org.scalajs.nodejs.mongodb.{FindAndUpdateOptions, _}

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Routes Package Object
  * @author lawrence.daniels@gmail.com
  */
package object routes {

  type NextFunction = js.Function0[Unit]

  /**
    * Data Access Object Extensions
    * @author lawrence.daniels@gmail.com
    */
  implicit class DAOExtensions(val coll: Collection) extends AnyVal {

    @inline
    def findById[T <: js.Any](id: String)(implicit mongo: MongoDB, ec: ExecutionContext) = {
      coll.findOneFuture[T]("_id" $eq id.$oid)
    }

    @inline
    def findById[T <: js.Any](id: String, fields: js.Array[String])(implicit mongo: MongoDB, ec: ExecutionContext) = {
      coll.findOneFuture[T]("_id" $eq id.$oid, fields)
    }

    @inline
    def follow(entityID: String, userID: String)(implicit mongo: MongoDB) = {
      link(entityID, userID, entitySetName = "followers", entityQtyName = "totalFollowers")
    }

    @inline
    def unfollow(entityID: String, userID: String)(implicit mongo: MongoDB) = {
      unlink(entityID, userID, entitySetName = "followers", entityQtyName = "totalFollowers")
    }

    @inline
    def like(entityID: String, userID: String)(implicit mongo: MongoDB) = {
      link(entityID, userID, entitySetName = "likedBy", entityQtyName = "likes")
    }

    @inline
    def unlike(entityID: String, userID: String)(implicit mongo: MongoDB) = {
      unlink(entityID, userID, entitySetName = "likedBy", entityQtyName = "likes")
    }

    private def link(entityID: String, userID: String, entitySetName: String, entityQtyName: String)(implicit mongo: MongoDB) = {
      coll.findOneAndUpdate(
        filter = doc("_id" $eq mongo.ObjectID(entityID), $or(entitySetName $nin js.Array(userID), entitySetName $exists false)),
        update = doc(entitySetName $addToSet userID, entityQtyName $inc 1),
        options = new FindAndUpdateOptions(upsert = false, returnOriginal = false)
      ).toFuture
    }

    private def unlink(entityID: String, userID: String, entitySetName: String, entityQtyName: String)(implicit mongo: MongoDB) = {
      coll.findOneAndUpdate(
        filter = doc("_id" $eq mongo.ObjectID(entityID), entitySetName $in js.Array(userID)),
        update = doc(entitySetName $pull userID, entityQtyName $inc -1),
        options = new FindAndUpdateOptions(upsert = false, returnOriginal = false)
      ).toFuture
    }
  }

  /**
    * Parameter Extensions
    * @author lawrence.daniels@gmail.com
    */
  implicit class ParameterExtensions(val params: js.Dictionary[String]) extends AnyVal {

    @inline
    def extractParams(names: String*) = {
      val values = names.map(params.get)
      if (values.forall(_.isDefined)) Some(values.flatten) else None
    }
  }

  /**
    * Response Extensions
    * @author lawrence.daniels@gmail.com
    */
  implicit class ResponseExtensions(val response: Response) extends AnyVal {

    @inline
    def missingParams(params: String*) = {
      val message = s"Bad Request: ${params.mkString(" and ")} ${if (params.length == 1) "is" else "are"} required"
      response.status(400).send(message)
    }

    @inline
    def sendCSV(workloads: js.Array[Workload]) = {
      response.setContentType("text/csv")
      response.writeHead(200)
      response.end(toCSVData(workloads))
    }

    private def toCSVData[T](workloads: js.Array[_ <: WorkloadLike[T]]) = {
      val headings = workloads.headOption.map(_.asKeyValues).map(_.keys.filterNot(_ == "_id")) getOrElse Nil
      val data = workloads map { workload =>
        headings.map(k => asString(workload.asKeyValues(k))).mkString(",")
      }
      data.prepend(headings.map(asString(_)).mkString(","))
      data.mkString("\n")
    }

    private def asString(value: js.Any): String = value match {
      case v if v == null => ""
      case v if js.typeOf(v) == "string" => s""""${v.toString}""""
      case v => v.toString
    }

  }

}
