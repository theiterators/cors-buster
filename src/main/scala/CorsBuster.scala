import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{Host, RawHeader}
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
      .mapHeaders(headers => headers.filterNot(_.lowercaseName() == "host"))
      .addHeader(Host(config.serverHost, config.serverPort))
  }
  val outgoingConnection = Http().outgoingConnection(config.serverHost, config.serverPort)
  val responseFlow = Flow.fromFunction[HttpResponse, HttpResponse] { response =>
    response
      .withHeaders(
        RawHeader("Access-Control-Allow-Origin", "*"),
        RawHeader("Access-Control-Allow-Credentials", "true"),
        RawHeader("Access-Control-Allow-Methods", "POST, PUT, DELETE, GET, OPTIONS"),
        RawHeader("Access-Control-Request-Method", "POST, PUT, DELETE, GET, OPTIONS"),
        RawHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization, DNT, X-CustomHeader, Keep-Alive, " +
          "User-Agent, X-Requested-With, If-Modified-Since, Cache-Control")
      )
  }

  Http().bindAndHandle(requestFlow via outgoingConnection via responseFlow, config.proxyHost, config.proxyPort)
}
