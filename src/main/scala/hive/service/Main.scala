package hive.service

import cats.effect.kernel.Resource
import cats.effect.{ExitCode, IO, IOApp}
import com.typesafe.scalalogging.LazyLogging
import doobie.util.transactor.Transactor
import hive.service.files.{FilesApi, FilesRepository, FilesService}
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import org.http4s.server.Server

object Main extends IOApp with LazyLogging {

  case class AppConfig(db: DbConfig, vipFilesPath: String)

  override def run(args: List[String]): IO[ExitCode] = {
    val vc = new VipCache
    val resources = for {
      conf <- loadConfig()
      tx   <- DbTransactor(conf.db)
      -    <- createServer(tx, vc)
      _    <- vc.load(conf.vipFilesPath)
      _    <- runDbMigrations(conf.db)
    } yield {}

    resources.useForever
      .as(ExitCode.Success)
  }

  private def loadConfig(): Resource[IO, AppConfig] = {
    Resource.eval(IO {
      AppConfig(
        db = DbConfig(
          user = sys.env("DB_USER"),
          pass = sys.env("DB_PASS"),
          host = sys.env("DB_HOST"),
          port = sys.env("DB_PORT").toInt,
          database = sys.env("DB_NAME")
        ),
        vipFilesPath = sys.env("VIP_CONFIG_PATH")
      )
    })
  }

  private def runDbMigrations(config: DbConfig): Resource[IO, MigrateResult] = {
    Resource.eval(IO {
      logger.info("Starting DB migration...")
      Flyway.configure()
        .connectRetries(10)
        .dataSource(s"jdbc:postgresql://${config.host}:${config.port}/${config.database}", config.user, config.pass)
        .load()
        .migrate()
    })
  }

  private def createServer(tx: Transactor[IO], vc: VipCache): Resource[IO, Server] = {
    logger.info("Bootstrapping service...")

    implicit val vipCache: VipCache = vc

    implicit lazy val dbExec: DbExecutor = new DbExecutor(tx)

    implicit lazy val filesRepo: FilesRepository = new FilesRepository
    implicit lazy val filesService: FilesService = new FilesService
    implicit lazy val filesApi: FilesApi         = new FilesApi

    implicit lazy val healthApi: HealthApi = new HealthApi

    ServerBuilder(filesApi, healthApi)
  }

}


