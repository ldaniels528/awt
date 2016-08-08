package com.microsoft.awt.directives

import com.microsoft.awt.components.SessionFactory
import org.scalajs.angularjs.Directive._
import org.scalajs.angularjs.sanitize.Sce
import org.scalajs.angularjs.{Attributes, Compile, Directive, JQLite, Scope, injected}

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Censor Directive
  * @author lawrence.daniels@gmail.com
  * @example {{{ <censor text="{{ myText }}"></censor> }}}
  */
class CensorDirective($compile: Compile, $sce: Sce, @injected("SessionFactory") sessionFactory: SessionFactory) extends Directive
  with ElementSupport with EmoticonSupport with LinkSupport[CensorDirectiveScope] with TemplateSupport {

  private val censor_block = """<span class="sk_censored">censored</span>"""

  override val scope = new CensorDirectiveScopeTemplate(text = "@text")
  override val replace = true
  override val transclude = true
  override val template = """<span compile="html"></span>"""

  override def link(scope: CensorDirectiveScope, element: JQLite, attrs: Attributes): Unit = {
    scope.html = scope.text map replaceTags map enrichWithEmoticons
  }

  private def replaceTags(text: String) = {
    val isAnonymous = sessionFactory.session.flatMap(_.isAnonymous).getOrElse(true)
    val sb = new StringBuilder(text)

    var lastPos = -1
    do {
      val start = sb.indexOf("[[", lastPos)
      val end = sb.indexOf("]]", start)
      if (start != -1 && end != -1) {
        val limit = end + 2
        if (isAnonymous) sb.replace(start, limit, censor_block)
        else sb.replace(start, limit, sb.substring(start, limit).drop(2).dropRight(2))
        lastPos = end
      }
      else lastPos = -1
    } while (lastPos != -1)
    sb.toString()
  }

}

/**
  * Censor Directive Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait CensorDirectiveScope extends Scope {
  // input fields
  var text: js.UndefOr[String] = js.native

  /// output fields
  var html: js.UndefOr[String] = js.native
}

/**
  * Censor Directive Scope Template
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class CensorDirectiveScopeTemplate(val text: String) extends js.Object