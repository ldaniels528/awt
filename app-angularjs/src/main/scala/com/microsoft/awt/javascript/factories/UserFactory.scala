package com.microsoft.awt.javascript.factories

import com.microsoft.awt.javascript.forms.ProfileEditForm
import com.microsoft.awt.javascript.models.{Post, Submitter, User}
import com.microsoft.awt.javascript.services.UserService
import org.scalajs.angularjs.AngularJsHelper._
import org.scalajs.angularjs._
import org.scalajs.dom.browser.console

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
  * User Caching Factory
  * @author lawrence.daniels@gmail.com
  */
class UserFactory(@injected("UserService") userService: UserService) extends Factory {
  private val cache = js.Dictionary[Future[User]]()

  // preload all users
  userService.getUsers() onComplete {
    case Success(users) =>
      console.info(s"Pre-loaded ${users.length} users")
      users.foreach(u => u._id.foreach(id => cache(id) = Future.successful(u)))
    case Failure(e) =>
      console.error(s"Failed to load all users: ${e.displayMessage}")
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Enrichment Functions
  ///////////////////////////////////////////////////////////////////////////

  /**
    * Asynchronously enriches the given posts with submitter instances
    * @param posts the given posts
    * @param ec    the given [[ExecutionContext execution context]]
    * @return a promise of the enriched posts
    */
  def enrich(posts: js.Array[Post])(implicit ec: ExecutionContext) = {
    val submitterIds = posts.flatMap(_.submitterId.toOption)
    for {
      submitters <- getUsers(submitterIds)
      submitterMap = Map(submitters.map(s => s._id.orNull -> Submitter(s)): _*)
    } yield {
      posts.foreach(post => post.submitter = post.submitterId.flatMap(submitterMap.get(_).orUndefined))
      posts
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //      CRUD Functions
  ///////////////////////////////////////////////////////////////////////////

  /**
    * Synchronously retrieves a user instance for the given user ID
    * @param userId the given user ID
    * @return an option of the user instance
    */
  def findUserByID(userId: String) = cache.get(userId).flatMap(_.value.flatMap(_.toOption))

  /**
    * Asynchronously retrieves a user instance for the given user ID
    * @param userId the given user ID
    * @param ec     the given [[ExecutionContext execution context]]
    * @return a promise of the user instance
    */
  def getUserByID(userId: String)(implicit ec: ExecutionContext) = {
    cache.getOrElseUpdate(userId, {
      console.log(s"Loading user information for # $userId...")
      val promise = userService.getUserByID(userId)
      promise onFailure { case e =>
        console.log(s"Unexpected failure: ${e.displayMessage}")
        cache.delete(userId)
      }
      promise
    })
  }

  /**
    * Retrieves a set of users by ID
    * @param userIds the given user IDs
    * @param ec      the given [[ExecutionContext execution context]]
    * @return the array of [[User users]]
    */
  def getUsers(userIds: js.Array[String])(implicit ec: ExecutionContext) = {
    Future.sequence(userIds.toSeq map getUserByID) map (seq => js.Array(seq: _*))
  }

  /**
    * Updates the user via the given profile edit form
    * @param profile the given profile edit form
    * @return a promise of an updated [[ProfileEditForm user]]
    */
  def updateUser(profile: ProfileEditForm)(implicit ec: ExecutionContext) = {
    val promise = userService.updateUser(profile)
    promise foreach { user =>
      user._id.foreach(id => cache(id) = promise)
    }
    promise
  }

}
