package hive.service

import cats.effect.{IO, Resource}
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.{Router, Server}
import sttp.tapir.server.http4s.Http4sServerInterpreter


object ServerBuilder {

  def apply(apis: BaseApi*): Resource[IO, Server] = apply(apis.toList)

  def apply(apis: List[BaseApi]): Resource[IO, Server] = {

    val docs: HttpRoutes[IO] = DocsApi(apis.flatMap(_.endpoints))

    val routes: HttpRoutes[IO] = apis.map(_(Http4sServerInterpreter[IO]())).reduce(_ <+> _)

    val router: HttpRoutes[IO] = Router(
      "/" -> routes,
      "/docs" -> docs
    )

    BlazeServerBuilder[IO]
      .bindHttp(host = "0.0.0.0", port = 80)
      .withHttpApp(router.orNotFound)
      .resource
  }

}
