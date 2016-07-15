package com.microsoft.awt.javascript.services

import com.microsoft.awt.javascript.models.EntitySearchResult
import org.scalajs.angularjs.Service
import org.scalajs.angularjs.http.Http

import scala.scalajs.js

/**
  * Reactive Search Service
  * @author lawrence.daniels@gmail.com
  */
class ReactiveSearchService($http: Http) extends Service {

  def search(searchTerm: String, maxResults: Int = 20) = {
    $http.get[js.Array[EntitySearchResult]](s"/api/search?searchTerm=$searchTerm&maxResults=$maxResults")
  }

}
