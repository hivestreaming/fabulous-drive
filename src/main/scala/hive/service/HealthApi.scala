package hive.service
import cats.effect.IO
import cats.implicits._
import org.http4s.HttpRoutes
import sttp.model.StatusCode
import sttp.tapir.Endpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import doobie.implicits._

class HealthApi(implicit dbExec: DbExecutor) extends BaseApi {

  private val health: Endpoint[Unit, Unit, Unit, Unit, Any] =
    endpoint
      .tag("status")
      .in("health")
      .out(statusCode(StatusCode.Ok))

  private val dbup: Endpoint[Unit, Unit, AppError, Unit, Any] =
    baseEndpoint
      .tag("status")
      .in("db_up")
      .out(statusCode(StatusCode.Ok))

  private def healthLogic(u: Unit): IO[Unit] = {
    IO.pure({})
  }

  private def dbupLogic(u: Unit): IO[Either[AppError, Unit]] = {
    dbExec.exec(sql"SELECT 1".query[Int].unique).as({}).value
  }

  override val endpoints: List[AnyEndpoint] = List(health, dbup)

  override def apply(interpreter: Http4sServerInterpreter[IO]): HttpRoutes[IO] =
    interpreter.toRoutes(List(
      health.serverLogicSuccess(healthLogic),
      dbup.serverLogic(dbupLogic)
    ))

}
