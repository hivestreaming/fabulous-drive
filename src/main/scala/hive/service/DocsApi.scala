package hive.service

import cats.effect.IO
import org.http4s.HttpRoutes
import sttp.tapir.Endpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.SwaggerUIOptions
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object DocsApi {

  private val swaggerUiOptions: SwaggerUIOptions = SwaggerUIOptions.default.copy(
    pathPrefix = List("service")
  )

  def apply(endpoints: List[Endpoint[_, _, _, _, Any]]): HttpRoutes[IO] =
    Http4sServerInterpreter[IO]()
      .toRoutes {
        SwaggerInterpreter(swaggerUIOptions = swaggerUiOptions)
          .fromEndpoints[IO](endpoints, title = "Files API", version = "1.0.0")
      }

}
