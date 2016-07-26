package com.microsoft.awt.components

import com.microsoft.awt.models.Group
import org.scalajs.angularjs.Service
import org.scalajs.angularjs.http.Http
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Groups Service
  * @author lawrence.daniels@gmail.com
  */
class GroupService($http: Http) extends Service {

  /**
    * Creates a new group
    * @param group the given [[Group group]]
    * @return the newly created group
    */
  def createGroup(group: Group) = $http.post[Group]("/api/group")

  /**
    * Retrieves a group by ID
    * @param groupID the given group ID
    * @return a promise of a [[com.microsoft.awt.models.Group group]]
    */
  def getGroupByID(groupID: String) = $http.get[Group](s"/api/group/$groupID")

  /**
    * Retrieves all groups
    * @param maxResults the maximum number of results to return
    * @return a promise of an array of [[com.microsoft.awt.models.Group groups]]
    */
  def getGroups(maxResults: Int = 20) = $http.get[js.Array[Group]](s"/api/groups?maxResults=$maxResults")

  /**
    * Retrieves all groups that are owned by or include a specific user
    * @param userID     the given member (user) ID
    * @param maxResults the maximum number of results to return
    * @return a promise of an array of [[com.microsoft.awt.models.Group groups]]
    */
  def getGroupsOwnedByOrIncludeUser(userID: String, maxResults: Int = 100)(implicit ec: ExecutionContext) = {
    getGroups(maxResults).map(_.filter(group => group.owner.contains(userID) || group.members.exists(_.contains(userID))))
  }

  /**
    * Retrieves all groups that include a specific user
    * @param userID     the given member (user) ID
    * @param maxResults the maximum number of results to return
    * @return a promise of an array of [[com.microsoft.awt.models.Group groups]]
    */
  def getGroupsIncludingUser(userID: String, maxResults: Int = 20) = {
    $http.get[js.Array[Group]](s"/api/groups/user/$userID/in?maxResults=$maxResults")
  }

  /**
    * Retrieves all groups that do not contain a specific user
    * @param userID     the given member (user) ID
    * @param maxResults the maximum number of results to return
    * @return a promise of an array of [[com.microsoft.awt.models.Group groups]]
    */
  def getGroupsExcludingUser(userID: String, maxResults: Int = 20) = {
    $http.get[js.Array[Group]](s"/api/groups/user/$userID/nin?maxResults=$maxResults")
  }

  /**
    * Updates an existing group
    * @param group the given [[Group group]]
    * @return the updated group
    */
  def updateGroup(group: Group) = $http.put[Group](s"/api/group/${group._id}")

}
