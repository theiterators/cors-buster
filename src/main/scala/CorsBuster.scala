import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow

import scala.util.Try

object CorsBuster extends App {

  case class Config(proxyHost: String, proxyPort: Int, serverHost: String, serverPort: Int)

  // TODO: accept different formats
  val configOpt = Try(Config(args(0), args(1).toInt, args(2), args(3).toInt)).toOption
  val config = configOpt.getOrElse {
    println("Usage java -jar cors-buster.jar proxyHost proxyPort serverHost serverPort\n" +
      "Ex. java -jar cors-buster.jar 0.0.0.0 8080 localhost 9000")
    sys.exit(-1)
  }

  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val requestFlow = Flow.fromFunction[HttpRequest, HttpRequest] { request =>
    request
      .withUri(request.uri.withAuthority(config.serverHost, config.serverPort))
      .mapHeaders(headers => headers.filterNot(_.lowercaseName() == Host.lowercaseName))
      .addHeader(Host(config.serverHost, config.serverPort))
  }
  val outgoingConnection = Http().outgoingConnection(config.serverHost, config.serverPort)
  val responseFlow = Flow.fromFunction[HttpResponse, HttpResponse] { response =>
    response
      .withHeaders(`Access-Control-Allow-Origin`.*)
  }

  Http().bindAndHandle(requestFlow via outgoingConnection via responseFlow, config.proxyHost, config.proxyPort)
}
