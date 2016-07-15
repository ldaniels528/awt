package com.microsoft.awt.javascript.services

import com.microsoft.awt.javascript.models.Workload
import com.microsoft.awt.javascript.services.WorkloadService.WorkloadUpdateResult
import org.scalajs.angularjs.Service
import org.scalajs.angularjs.http.Http

import scala.scalajs.js

/**
  * Workload Service
  * @author lawrence.daniels@gmail.com
  */
class WorkloadService($http: Http) extends Service {

  ///////////////////////////////////////////////////////////////////////////
  //      Workload Functions
  ///////////////////////////////////////////////////////////////////////////

  def createWorkload(workload: Workload) = $http.post[Workload]("/api/workload", data = workload)

  def getWorkloadByID(workloadID: String) = $http.get[Workload](s"/api/workload/$workloadID")

  def getWorkloads(activeOnly: Boolean = true) = {
    $http.get[js.Array[Workload]](s"/api/workloads?activeOnly=$activeOnly")
  }

  def getWorkloadsByGroup(groupID: String, activeOnly: Boolean = true) = {
    $http.get[js.Array[Workload]](s"/api/workloads/group/$groupID?activeOnly=$activeOnly")
  }

  def getWorkloadsByUser(userID: String, activeOnly: Boolean = true) = {
    $http.get[js.Array[Workload]](s"/api/workloads/user/$userID?activeOnly=$activeOnly")
  }

  def updateWorkload(workload: Workload) = $http.put[WorkloadUpdateResult](s"/api/workload/${workload._id}", data = workload)

  ///////////////////////////////////////////////////////////////////////////
  //      Workload Download Functions
  ///////////////////////////////////////////////////////////////////////////

  def downloadWorkloads(fileName: String) = {
    $http.get[js.Array[Workload]](s"/api/workloads/download/$fileName")
  }

  def downloadWorkloadsByGroup(groupID: String, fileName: String) = {
    $http.get[js.Array[Workload]](s"/api/workloads/download/$groupID/group/$fileName")
  }

  def downloadWorkloadsByUser(userID: String, fileName: String) = {
    $http.get[js.Array[Workload]](s"/api/workloads/download/$userID/user/$fileName")
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Workload State Functions
  ///////////////////////////////////////////////////////////////////////////

  def deactivateWorkload(workloadID: String) = $http.delete[WorkloadUpdateResult](s"/api/workload/$workloadID/active")

  def reopenWorkload(workloadID: String) = $http.put[WorkloadUpdateResult](s"/api/workload/$workloadID/active")

  ///////////////////////////////////////////////////////////////////////////
  //      Workload Status Functions
  ///////////////////////////////////////////////////////////////////////////

  def createStatus(workloadID: String, status: Workload.Status) = $http.post[Workload](s"/api/workload/$workloadID/status", data = status)

  def deleteStatus(workloadID: String, statusID: String) = $http.delete[Workload](s"/api/workload/$workloadID/status/$statusID")

}

/**
  * Workload Service Companion
  * @author lawrence.daniels@gmail.com
  */
object WorkloadService {

  /**
    * Workload Update Result
    * @author lawrence.daniels@gmail.com
    */
  @js.native
  trait WorkloadUpdateResult extends js.Object {
    var nModified: Int = js.native
    var ok: Int = js.native
    var n: Int = js.native
  }

}