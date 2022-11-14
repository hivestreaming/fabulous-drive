package hive.service

import cats.effect.IO
import io.circe.generic.AutoDerivation
import org.http4s.HttpRoutes
import sttp.model.StatusCode
import sttp.tapir.{Endpoint, Tapir}
import sttp.tapir.json.circe.TapirJsonCirce
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.generic.auto.schemaForCaseClass

trait BaseApi extends Tapir with TapirJsonCirce with AutoDerivation {

  type AnyEndpoint = Endpoint[_, _, _, _, Any]

  protected val baseEndpoint: Endpoint[Unit, Unit, AppError, Unit, Any] =
    endpoint.errorOut(
      oneOf[AppError](
        oneOfVariant(StatusCode.Unauthorized, jsonBody[NotAuthorizedError]),
        oneOfVariant(StatusCode.NotFound, jsonBody[NotFoundError]),
        oneOfVariant(StatusCode.InternalServerError, jsonBody[DbError])
      )
    )

  val endpoints: List[AnyEndpoint]

  def apply(interpreter: Http4sServerInterpreter[IO]): HttpRoutes[IO]

}
