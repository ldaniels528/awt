package com.microsoft.awt.directives

import com.microsoft.awt.directives.ThumbnailDirective._
import org.scalajs.angularjs.Directive._
import org.scalajs.angularjs.{Attributes, Directive, JQLite, Scope}
import org.scalajs.dom
import org.scalajs.dom.browser._
import org.scalajs.dom.canvas._
import org.scalajs.nodejs.util.ScalaJsHelper._
import org.scalajs.sjs.JsUnderOrHelper._

import scala.scalajs.js

/**
  * Thumbnail Directive
  * @author lawrence.daniels@gmail.com
  */
class ThumbnailDirective($window: Window) extends Directive with AttributeRestriction with LinkSupport[Scope] with TemplateSupport {

  override val template = "<canvas/>"

  override def link(scope: Scope, element: JQLite, attributes: Attributes) = if (isSupported) {
    val params = scope.$eval[Params](attributes.ngThumb)
    params.file.flat foreach { file =>
      if (isSupportedType(file)) {
        // get the canvas instance
        val canvas = element.find[HTMLCanvasElement]("canvas")

        // setup the file reader
        val reader = new FileReader()
        reader.readAsDataURL(file)
        reader.onload = (event: dom.Event) => {
          val image = new Image()
          image.src = event.target.result
          image.onload = () => {
            val width = params.width.getOrElse(image.width / image.height * params.height.getOrElse(1d))
            val height = params.height.getOrElse(image.height / image.width * params.width.getOrElse(1d))
            canvas.attr(new CanvasAttributeOptions(width = width, height = height))
            canvas(0).get2DContext().drawImage(image, 0, 0, width, height)
          }
        }
      }
    }
  }

  private def isSupportedType(file: File) = file.`type`.startsWith("image/")

  private def isSupported = $window.FileReader.nonEmpty && $window.CanvasRenderingContext2D.nonEmpty

}

/**
  * Thumbnail Directive Companion
  * @author lawrence.daniels@gmail.com
  */
object ThumbnailDirective {

  /**
    * Thumbnail Directive Parameters
    * @author lawrence.daniels@gmail.com
    */
  @js.native
  trait Params extends js.Object {
    var file: js.UndefOr[File] = js.native
    var width: js.UndefOr[Double] = js.native
    var height: js.UndefOr[Double] = js.native
  }

  /**
    * Window with CanvasRenderingContext2D, File and FileReader
    * @author lawrence.daniels@gmail.com
    */
  @js.native
  trait Window extends org.scalajs.dom.Window {
    var CanvasRenderingContext2D: js.UndefOr[CanvasRenderingContext2D] = js.native
    var File: js.UndefOr[File] = js.native
    var FileReader: js.UndefOr[FileReader] = js.native
  }

  /**
    * Event Extensions
    * @param target the given [[dom.EventTarget event target]]
    */
  final implicit class EventExtensions(val target: dom.EventTarget) extends AnyVal {

    @inline
    def result = target.dynamic.result.asInstanceOf[String]

  }

  /**
    * Thumbnail Extensions
    * @param attributes the given [[Attributes attributes]]
    */
  final implicit class ThumbnailExtensions(val attributes: Attributes) extends AnyVal {

    @inline
    def ngThumb = attributes.dynamic.ngThumb

  }

}
