package com.microsoft.awt.javascript.services

import com.microsoft.awt.javascript.forms.ProfileEditForm
import com.microsoft.awt.javascript.models.{OperationResult, Submitter, User}
import org.scalajs.angularjs.Service
import org.scalajs.angularjs.http.Http

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Users Service
  * @author lawrence.daniels@gmail.com
  */
class UserService($http: Http) extends Service {

  /////////////////////////////////////////////////////////////////////////////////
  //      User CRUD
  /////////////////////////////////////////////////////////////////////////////////

  /**
    * Retrieves a user by ID
    * @param userID the given user ID
    * @return a promise of a [[com.microsoft.awt.javascript.models.User user]]
    */
  def getUserByID(userID: String) = $http.get[User](s"/api/user/$userID")

  /**
    * Retrieves a user via primary email (e.g. "lawrence.daniels@gmail.com")
    * @param primaryEmail the given primary email address
    * @return a promise of a [[com.microsoft.awt.javascript.models.User user]]
    */
  def getUserByEmail(primaryEmail: String) = $http.get[User](s"/api/users/email?primaryEmail=$primaryEmail")

  /**
    * Retrieves a user via username (e.g. "daniell_x@yahoo.com")
    * @param username the given username
    * @return a promise of a [[com.microsoft.awt.javascript.models.User user]]
    */
  def getUserByName(username: String) = $http.get[User](s"/api/users/name?username=$username")

  /**
    * Retrieves all users
    * @return the array of [[User users]]
    */
  def getUsers() = $http.get[js.Array[User]]("/api/users")

  /**
    * Retrieves a set of users by ID
    * @param userIds the given user IDs
    * @return the array of [[User users]]
    */
  def getUsers(userIds: js.Array[String]) = $http.get[js.Array[User]](s"/api/users?ids=${userIds.mkString("+")}")

  /**
    * Updates the user via the given profile edit form
    * @param profile the given profile edit form
    * @return a promise of an updated [[ProfileEditForm user]]
    */
  def updateUser(profile: ProfileEditForm) = $http.put[User](s"/api/user/${profile._id}", profile)

  /////////////////////////////////////////////////////////////////////////////////
  //      Avatars, etc.
  /////////////////////////////////////////////////////////////////////////////////

  /**
    * Retrieves an avatar image by user ID
    * @param userID the given user ID
    * @return a promise of an image
    */
  def getAvatar(userID: String)(implicit ec: ExecutionContext) = {
    $http.get[js.Any](s"/api/user/$userID/avatar")
  }

  /**
    * Retrieves the submitter representation of a user
    * @param userID the given submitter ID
    * @return a promise of a [[Submitter submitter]]
    */
  def getSubmitter(userID: String)(implicit ec: ExecutionContext) = {
    $http.get[Submitter](s"/api/user/$userID/submitter")
  }

  /////////////////////////////////////////////////////////////////////////////////
  //      Endorsers (Like / UnLike)
  /////////////////////////////////////////////////////////////////////////////////

  /**
    * Facilitates the endorsement a user's skill
    * @param userID     the ID of the user to endorse
    * @param endorserID the ID of the user to providing the endorsement
    * @return a promise of a [[com.microsoft.awt.javascript.models.OperationResult result]]
    */
  def like(userID: String, endorserID: String)(implicit ec: ExecutionContext) = {
    $http.put[OperationResult](s"/api/user/$userID/like/$endorserID")
  }

  /**
    * Facilitates the endorsement a user's skill
    * @param userID     the ID of the user to endorse
    * @param endorserID the ID of the user to providing the endorsement
    * @return a promise of a [[com.microsoft.awt.javascript.models.OperationResult result]]
    */
  def unlike(userID: String, endorserID: String)(implicit ec: ExecutionContext) = {
    $http.put[OperationResult](s"/api/user/$userID/like/$endorserID")
  }

  /////////////////////////////////////////////////////////////////////////////////
  //      Followers (Follow / UnFollow)
  /////////////////////////////////////////////////////////////////////////////////

  /**
    * Facilitates following the activities of a user
    * @param userID     the ID of the user to endorse
    * @param followerID the ID of the user doing the following
    * @return a promise of a [[com.microsoft.awt.javascript.models.OperationResult result]]
    */
  def follow(userID: String, followerID: String)(implicit ec: ExecutionContext) = {
    $http.put[OperationResult](s"/api/user/$userID/follow/$followerID")
  }

  /**
    * Retrieves the collection of followers for a given user
    * @param followeeID the ID of the user being followed
    * @return a promise of an array of followers
    */
  def getFollowers(followeeID: String)(implicit ec: ExecutionContext) = {
    $http.get[js.Array[User]](s"/api/user/$followeeID/followers")
  }

  /**
    * Facilitates un-following the activities of a user
    * @param userID     the ID of the user to endorse
    * @param followerID the ID of the user doing the following
    * @return a promise of a [[com.microsoft.awt.javascript.models.OperationResult result]]
    */
  def unfollow(userID: String, followerID: String)(implicit ec: ExecutionContext) = {
    $http.delete[OperationResult](s"/api/user/$userID/follow/$followerID")
  }

}
