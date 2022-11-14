package hive.service.files

import java.time.LocalDateTime

import cats.data.EitherT
import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import doobie.implicits._
import doobie.util.log.LogHandler
import doobie.util.meta.Meta
import hive.service.{AppError, DbExecutor}

class FilesRepository(implicit db: DbExecutor) extends LazyLogging {

  implicit val tm: Meta[LocalDateTime] = doobie.implicits.javatimedrivernative.JavaTimeLocalDateTimeMeta

  implicit val logHandler: LogHandler = LogHandler { message =>
    logger.debug(s"SQL Query: $message")
  }

  private val TableName = fr"files"

  def get(id: String): EitherT[IO, AppError, Option[FileModel]] =
    db.exec {
      sql"SELECT id, path, name, created_at, created_by, content FROM $TableName where id = $id"
        .query[FileModel]
        .option
    }

  def getDir(path: String): EitherT[IO, AppError, Seq[DirectoryItem]] =
    db.exec {
      sql"SELECT id, name FROM $TableName WHERE path = $path"
        .query[DirectoryItem]
        .to[Seq]
    }

  def put(file: FileModel): EitherT[IO, AppError, Int] =
    db.exec {
      sql"""INSERT INTO $TableName
           |VALUES (
           |  ${file.id},
           |  ${file.path},
           |  ${file.name},
           |  ${file.createdAt},
           |  ${file.createdBy},
           |  ${file.content}
           |)""".stripMargin
        .update
        .run
    }

}
