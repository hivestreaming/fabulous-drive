package hive.service

import java.util.concurrent.TimeUnit.MINUTES

import cats.data.EitherT
import cats.effect.IO
import cats.effect.kernel.Resource
import com.typesafe.scalalogging.LazyLogging
import com.zaxxer.hikari.HikariConfig
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.ExecutionContexts
import doobie.{ConnectionIO, Transactor}

object DbTransactor extends LazyLogging {

  private val ThreadPoolSize: Int = 10

  def apply(config: DbConfig): Resource[IO, HikariTransactor[IO]] = {
    logger.debug("Requested new db transactor")
    for {
      ec <- ExecutionContexts.fixedThreadPool[IO](ThreadPoolSize)
      xa <- HikariTransactor.fromHikariConfig[IO](mkHikariConfig(config), ec)
    } yield xa
  }

  private def mkHikariConfig(config: DbConfig): HikariConfig = {
    val hikari = new HikariConfig()
    hikari.setDriverClassName("org.postgresql.Driver")
    hikari.setJdbcUrl(s"jdbc:postgresql://${config.host}:${config.port}/${config.database}")
    hikari.setUsername(config.user)
    hikari.setPassword(config.pass)
    hikari.setInitializationFailTimeout(MINUTES.toMillis(5))

    hikari
  }

}

case class DbConfig(
  host: String,
  port: Int,
  user: String,
  pass: String,
  database: String
)

class DbExecutor(tx: Transactor[IO]) {

  def exec[R](query: ConnectionIO[R]): EitherT[IO, AppError, R] = {
    EitherT(query.transact(tx).attempt)
      .leftMap(e => DbError(e.getMessage))
  }

}
