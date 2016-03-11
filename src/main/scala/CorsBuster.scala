import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpHeader, HttpMethods, HttpResponse, HttpRequest}
import akka.stream._
import akka.stream.scaladsl.{Merge, Broadcast, GraphDSL, Flow}

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

  val optionRequestFlow = Flow.fromFunction[HttpRequest, HttpResponse] { request =>
    request.method match {
      case OPTIONS =>
        val responseHeaders = request.headers.flatMap {
          case `Access-Control-Request-Method`(method) => Option(`Access-Control-Allow-Methods`(method))
          case `Access-Control-Request-Headers`(headers) => Option(`Access-Control-Allow-Headers`(headers))
          case _ => None
        }
        HttpResponse(NoContent, responseHeaders)
      case _ => throw new IllegalStateException("Flow handles only OPTIONS request method")
    }
  }
  val standardRequestFlow = Http().outgoingConnection(config.serverHost, config.serverPort)

  val responseFlow = Flow.fromFunction[HttpResponse, HttpResponse] { response =>
    response
      .addHeader(`Access-Control-Allow-Origin`.*)
      .addHeader(`Access-Control-Allow-Credentials`(true))
  }

  val requestResponseFlow = Flow.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val broadcast = b.add(Broadcast[HttpRequest](2))
    val merge = b.add(Merge[HttpResponse](2))
    val outFlow = b.add(Flow[HttpResponse])

    broadcast.filter(_.method == OPTIONS) ~> optionRequestFlow ~>   merge ~> responseFlow ~> outFlow
    broadcast.filter(_.method != OPTIONS) ~> standardRequestFlow ~> merge

    FlowShape(broadcast.in, outFlow.out)
  })

  Http().bindAndHandle(requestResponseFlow, config.proxyHost, config.proxyPort)
}
