package hive.service

import cats.effect.{IO, Resource}
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.{Router, Server}
import sttp.tapir.server.http4s.Http4sServerInterpreter


case class ServerConfig(host: String, port: Int)

object ServerBuilder {

  def apply(config: ServerConfig, apis: BaseApi*): Resource[IO, Server] = apply(config, apis.toList)

  def apply(config: ServerConfig, apis: List[BaseApi]): Resource[IO, Server] = {

    val docs: HttpRoutes[IO] = DocsApi(apis.flatMap(_.endpoints))

    val routes: HttpRoutes[IO] = apis.map(_(Http4sServerInterpreter[IO]())).reduce(_ <+> _)

    val router: HttpRoutes[IO] = Router(
      "/" -> routes,
      "/docs" -> docs
    )

    BlazeServerBuilder[IO]
      .bindHttp(host = config.host, port = config.port)
      .withHttpApp(router.orNotFound)
      .resource
  }

}
