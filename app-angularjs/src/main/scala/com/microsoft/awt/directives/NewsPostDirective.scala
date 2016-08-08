package com.microsoft.awt.directives

import org.scalajs.angularjs.Directive._
import org.scalajs.angularjs.sanitize.Sce
import org.scalajs.angularjs.{Attributes, Compile, Directive, JQLite, Scope}

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * News Post Directive
  * @author lawrence.daniels@gmail.com
  * @example <news-post text="{{ post.text }}"></news-post>
  */
class NewsPostDirective($compile: Compile, $sce: Sce) extends Directive
  with ElementSupport with EmoticonSupport with LinkSupport[NewsPostDirectiveScope] with TemplateSupport {

  override val scope = new NewsPostDirectiveScopeTemplate(text = "=", callback = "&")
  override val transclude = true
  override val replace = true
  override val template = """<span compile="html"></span>"""

  override def link(scope: NewsPostDirectiveScope, element: JQLite, attrs: Attributes) = {
    scope.html = scope.text map enrichHashTags map enrichWithEmoticons
  }

  private def enrichHashTags(text: String) = {
    if (text.contains('#')) {
      val sb = new StringBuilder(text)
      var lastPos = -1
      do {
        val start = sb.indexOf('#', lastPos)
        if (start != -1) {
          val end = sb.indexOf(' ', start)
          val limit = if (end != -1) end else sb.length
          val hashTag = sb.substring(start, limit)
          val tag = hashTag.tail
          val hashTagWithLink = s"""<a ng-click="callback({'tag': '$tag'})">$hashTag</a>"""

          sb.replace(start, limit, hashTagWithLink)
          lastPos = start + hashTagWithLink.length
        }
        else lastPos = -1
      } while (lastPos != -1 && lastPos < sb.length)

      sb.toString()
    }
    else text
  }

}

/**
  * News Post Directive Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait NewsPostDirectiveScope extends Scope {
  // input fields
  var text: js.UndefOr[String] = js.native
  var callback: js.UndefOr[js.Function] = js.native

  /// output fields
  var html: js.UndefOr[String] = js.native
}

/**
  * News Post Directive Scope Template
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class NewsPostDirectiveScopeTemplate(val text: String, val callback: String) extends js.Object