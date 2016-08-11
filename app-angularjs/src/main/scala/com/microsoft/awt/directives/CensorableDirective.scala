package com.microsoft.awt.directives

import com.microsoft.awt.components.{EmoticonSupport, SessionFactory}
import org.scalajs.angularjs.Directive._
import org.scalajs.angularjs.sanitize.Sce
import org.scalajs.angularjs.{Attributes, Directive, JQLite, Scope, injected}
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js

/**
  * Censorable Directive
  * @author lawrence.daniels@gmail.com
  * @example {{{ <censorable text="{{ myText }}"></censorable> }}}
  */
class CensorableDirective($sce: Sce, @injected("SessionFactory") sessionFactory: SessionFactory) extends Directive
  with ElementRestriction with EmoticonSupport with LinkSupport[CensorableDirectiveScope] with TemplateSupport {

  private val CensorBlock = """<span class="sk_censored">censored</span>"""
  private val SeqStart = "[["
  private val SeqEnd = "]]"

  override val scope = CensorableDirectiveScope(text = "@text")
  override val template = """<span ng-bind-html="html"></span>"""

  override def link(scope: CensorableDirectiveScope, element: JQLite, attrs: Attributes): Unit = {
    scope.$watch("text", (newText: js.UndefOr[String], oldText: js.UndefOr[String]) => {
      scope.html = newText.flat map replaceTags map enrichWithEmoticons
    })
  }

  private def replaceTags(text: String) = {
    val isAnonymous = sessionFactory.session.flatMap(_.isAnonymous).getOrElse(true)
    val sb = new StringBuilder(text)

    var lastPos = -1
    do {
      val start = sb.indexOf(SeqStart, lastPos)
      val end = sb.indexOf(SeqEnd, start + SeqStart.length)
      if (start != -1 && end != -1) {
        val limit = end + SeqEnd.length
        val replacement = if (isAnonymous) CensorBlock else sb.substring(start, limit).drop(SeqStart.length).dropRight(SeqEnd.length)
        sb.replace(start, limit, replacement)
        lastPos = end
      }
      else lastPos = -1
    } while (lastPos != -1)
    sb.toString()
  }

}

/**
  * Censorable Directive Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait CensorableDirectiveScope extends Scope {
  // input fields
  var text: js.UndefOr[String] = js.native

  /// output fields
  var html: js.UndefOr[String] = js.native
}

/**
  * Censorable Directive Scope Companion
  * @author lawrence.daniels@gmail.com
  */
object CensorableDirectiveScope {

  def apply(text: String) = {
    val scope = New[CensorableDirectiveScope]
    scope.text = text
    scope
  }

}
