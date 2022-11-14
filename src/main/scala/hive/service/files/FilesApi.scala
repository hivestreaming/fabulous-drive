package hive.service.files

import cats.effect.IO
import hive.service.{AppError, BaseApi}
import org.http4s.HttpRoutes
import sttp.model.StatusCode
import sttp.tapir.{Endpoint, PublicEndpoint}
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.server.http4s.Http4sServerInterpreter

class FilesApi(implicit service: FilesService) extends BaseApi {

  val getFile: PublicEndpoint[GetFileRequest, AppError, FileModel, Any] =
    baseEndpoint
      .tag("files")
      .in(header[String]("X-USER"))
      .in("file" / path[String]("id"))
      .mapInTo[GetFileRequest]
      .get
      .out(jsonBody[FileModel])

  val getDir: PublicEndpoint[GetFileRequest, AppError, Directory, Any] =
    baseEndpoint
      .tag("files")
      .in(header[String]("X-USER"))
      .in("files" and query[String]("path"))
      .mapInTo[GetFileRequest]
      .get
      .out(jsonBody[Directory])

  val createFile: PublicEndpoint[CreateFileRequest, AppError, Unit, Any] =
    baseEndpoint
      .tag("files")
      .in("files")
      .in(header[String]("X-USER"))
      .in(jsonBody[CreateFilePayload])
      .mapInTo[CreateFileRequest]
      .put
      .out(statusCode(StatusCode.Created))

  val endpoints: List[AnyEndpoint] =
    List(getFile, getDir, createFile)

  def apply(serverInterpreter: Http4sServerInterpreter[IO]): HttpRoutes[IO] =
    serverInterpreter.toRoutes(List(
      createFile.serverLogic[IO](service.create(_).value),
      getFile.serverLogic[IO](service.get(_).value),
      getDir.serverLogic[IO](service.getDir(_).value)
    ))

}
