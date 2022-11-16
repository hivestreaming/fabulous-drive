package hive.service.files

import java.time.LocalDateTime

import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import hive.service.{AppError, NotAuthorizedError, NotFoundError, VipCache}

class FilesService(implicit repo: FilesRepository, vc: VipCache) {

  def create(request: CreateFileRequest): EitherT[IO, AppError, Unit] = {
    val payload = request.payload
    val model = FileModel(
      id = IdGenerator.next(),
      path = payload.path,
      name = payload.name,
      createdAt = LocalDateTime.now,
      createdBy = request.user,
      content = payload.content
    )

    EitherT
      .cond[IO](
        vc.canAccessPath(request.user, request.payload.path),
        {},
        NotAuthorizedError()
      )
      .flatMap(_ => repo.put(model).as({}))

  }

  def get(request: GetFileRequest): EitherT[IO, AppError, FileModel] = {
    repo.get(request.path)
      .flatMap(EitherT.fromOption[IO](_, NotFoundError() : AppError))
      .flatMap(file => EitherT.cond[IO](
        vc.canAccessPath(request.user, file.path) && vc.canAccessFile(request.user, file.name),
        file,
        NotAuthorizedError()
      ))
  }

  def getDir(request: GetFileRequest): EitherT[IO, AppError, Directory] = {
    repo.getDir(request.path)
      .flatMap(file => EitherT.cond[IO](
        vc.canAccessPath(request.user, request.path),
        file,
        NotAuthorizedError() : AppError
      ))
      .flatMap {
        case Seq() => EitherT.leftT(NotFoundError())
        case files => EitherT.rightT(Directory(files))
      }
  }

}
