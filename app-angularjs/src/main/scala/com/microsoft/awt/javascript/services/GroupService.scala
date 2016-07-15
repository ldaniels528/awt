package com.microsoft.awt.javascript.services

import com.microsoft.awt.javascript.models.Group
import org.scalajs.angularjs.Service
import org.scalajs.angularjs.http.Http

import scala.scalajs.js

/**
  * Groups Service
  * @author lawrence.daniels@gmail.com
  */
class GroupService($http: Http) extends Service {

  /**
    * Retrieves a group by ID
    * @param groupID the given group ID
    * @return a promise of a [[com.microsoft.awt.javascript.models.Group group]]
    */
  def getGroupByID(groupID: String) = $http.get[Group](s"/api/group/$groupID")

  /**
    * Retrieves a group via name (e.g. "Scala.Js Developers")
    * @return a promise of a [[com.microsoft.awt.javascript.models.Group group]]
    */
  def getGroupByName(name: String) = $http.get[Group](s"/api/group?name=$name")

  /**
    * Retrieves all groups
    * @param maxResults the maximum number of results to return
    * @return a promise of an array of [[com.microsoft.awt.javascript.models.Group groups]]
    */
  def getGroups(maxResults: Int = 20) = $http.get[js.Array[Group]](s"/api/groups?maxResults=$maxResults")

  /**
    * Retrieves all groups that include a specific user
    * @param userID     the given member (user) ID
    * @param maxResults the maximum number of results to return
    * @return a promise of an array of [[com.microsoft.awt.javascript.models.Group groups]]
    */
  def getInclusiveGroups(userID: String, maxResults: Int = 20) = $http.get[js.Array[Group]](s"/api/groups/include/$userID?maxResults=$maxResults")

  /**
    * Retrieves all groups that do not contain a specific user
    * @param userID     the given member (user) ID
    * @param maxResults the maximum number of results to return
    * @return a promise of an array of [[com.microsoft.awt.javascript.models.Group groups]]
    */
  def getExclusiveGroups(userID: String, maxResults: Int = 20) = $http.get[js.Array[Group]](s"/api/groups/exclude/$userID?maxResults=$maxResults")

}
