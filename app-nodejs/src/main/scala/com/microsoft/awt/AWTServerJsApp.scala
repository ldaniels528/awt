package com.microsoft.awt

import com.microsoft.awt.routes._
import org.scalajs.nodejs._
import org.scalajs.nodejs.bodyparser._
import org.scalajs.nodejs.express.csv.ExpressCSV
import org.scalajs.nodejs.express.fileupload.ExpressFileUpload
import org.scalajs.nodejs.express.{Express, Request, Response}
import org.scalajs.nodejs.expressws.{ExpressWS, WsRouterExtensions}
import org.scalajs.nodejs.global._
import org.scalajs.nodejs.mongodb.MongoDB

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

/**
  * AWT Server Application
  * @author lawrence.daniels@gmail.com
  */
@JSExportAll
object AWTServerJsApp extends js.JSApp {

  override def main() = {}

  def startServer(implicit bootstrap: Bootstrap) {
    implicit val require = bootstrap.require

    // determine the port to listen on
    val port = process.env.get("port").map(_.toInt) getOrElse 1337
    val mongoServers = process.env.get("mongo_servers") getOrElse "localhost:27017"

    console.log("Loading Express modules...")
    implicit val express = Express()
    implicit val csv = ExpressCSV()
    implicit val app = express().withWsRouting
    implicit val wss = ExpressWS(app)
    implicit val fileUpload = ExpressFileUpload()

    console.log("Loading MongoDB module...")
    implicit val mongodb = MongoDB()

    // setup the body parsers
    console.log("Setting up body parsers...")
    val bodyParser = BodyParser()
    app.use(bodyParser.json())
    app.use(bodyParser.urlencoded(new UrlEncodedBodyOptions(extended = true)))

    // setup the routes for serving static files
    console.log("Setting up the routes for serving static files...")
    app.use(fileUpload())
    app.use(express.static("public"))
    app.use("/assets", express.static("public"))
    app.use("/bower_components", express.static("bower_components"))

    // disable caching
    app.disable("etag")

    // setup logging of the request - response cycles
    app.use((request: Request, response: Response, next: NextFunction) => {
      val startTime = System.currentTimeMillis()
      next()
      response.onFinish(() => {
        val elapsedTime = System.currentTimeMillis() - startTime
        console.log("[node] application - %s %s ~> %d [%d ms]", request.method, request.originalUrl, response.statusCode, elapsedTime)
      })
    })

    // setup mongodb connection
    val mongoUrl = s"mongodb://$mongoServers/msatool"
    console.log("Connecting to %s", mongoUrl)
    val dbFuture = mongodb.MongoClient.connectFuture(mongoUrl)

    // setup searchable entity routes
    EventRoutes.init(app, dbFuture)
    GroupRoutes.init(app, dbFuture)
    SearchRoutes.init(app, dbFuture)
    WorkloadRoutes.init(app, dbFuture)
    UserRoutes.init(app, dbFuture)

    // setup notification routes
    NotificationRoutes.init(app, dbFuture)
    WebSocketRoutes.init(app, wss, dbFuture)

    // setup post routes
    PostRoutes.init(app, dbFuture)

    // setup authentication/session routes
    AuthenticationRoutes.init(app, dbFuture)
    SessionRoutes.init(app, dbFuture)

    // catch any uncaught exceptions
    process.on("uncaughtException", (err: errors.Error) => {
      console.error(err.stack)
      console.log("Node NOT Exiting...")
    })

    // start the listener
    app.listen(port, () => console.log("Server now listening on port %d", port))
    ()
  }

}