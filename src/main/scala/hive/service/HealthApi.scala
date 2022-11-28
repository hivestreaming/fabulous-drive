package hive.service
import cats.data.EitherT
import cats.effect.IO
import doobie.implicits._
import org.http4s.HttpRoutes
import sttp.model.StatusCode
import sttp.tapir.Endpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter

class HealthApi(implicit dbExec: DbExecutor) extends BaseApi {

  private val health: Endpoint[Unit, Unit, Unit, Unit, Any] =
    endpoint
      .description("Returns OK if the server is up")
      .tag("status")
      .in("health")
      .out(statusCode(StatusCode.Ok))

  private val dbup: Endpoint[Unit, Unit, AppError, Unit, Any] =
    baseEndpoint
      .description("Checks if connection to database is OK")
      .tag("status")
      .in("db_up")
      .out(statusCode(StatusCode.Ok))

  private def healthLogic(u: Unit): IO[Unit] = {
    IO.pure({})
  }

  private def dbupLogic(u: Unit): IO[Either[AppError, Unit]] = {
    dbExec.exec(sql"SELECT name FROM cats WHERE id = 'N2u7EcJb'".query[String].option)
      .flatMap {
        case Some("noteworthy tourist") => EitherT.rightT[IO, AppError]({})
        case _ => EitherT.leftT[IO, Unit](DbError("Database not ready") : AppError)
      }
      .value
  }

  override val endpoints: List[AnyEndpoint] = List(health, dbup)

  override def apply(interpreter: Http4sServerInterpreter[IO]): HttpRoutes[IO] =
    interpreter.toRoutes(List(
      health.serverLogicSuccess(healthLogic),
      dbup.serverLogic(dbupLogic)
    ))

}
