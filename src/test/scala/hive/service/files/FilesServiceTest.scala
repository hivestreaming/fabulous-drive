package hive.service.files

import java.time.LocalDateTime

import cats.data.EitherT
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import hive.service.{AppError, NotAuthorizedError, NotFoundError, VipCache}
import org.mockito.scalatest.{IdiomaticMockito, ResetMocksAfterEachTest}
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.util.Random

class FilesServiceTest
  extends AnyFlatSpecLike
    with Matchers
    with IdiomaticMockito
    with ResetMocksAfterEachTest {

  private def run[R](body: => EitherT[IO, AppError, R]): Either[AppError, R] = body.value.unsafeRunSync()

  behavior of "FilesService"

  implicit val repo: FilesRepository = mock[FilesRepository]
  implicit val vc: VipCache = mock[VipCache]

  val service: FilesService = new FilesService

  it should "create a file" in new VipCacheOkMocks {
    repo.put(any[FileModel]).returns(EitherT.rightT[IO, AppError](1))

    val request = CreateFileRequest("potato", CreateFilePayload("/PotatoRealm/", "baked.txt", "Lorem ipsum dolor sit amet"))
    run(service.create(request)).shouldEqual(Right({}))
  }

  it should "fail to create a file if user is not VIP" in {
    repo.put(any[FileModel]).returns(EitherT.rightT[IO, AppError](1))
    vc.canAccessPath("potato", "/PotatoRealm/").returns(false)
    vc.canAccessFile("potato", "baked.txt").returns(true)

    val request = CreateFileRequest("potato", CreateFilePayload("/PotatoRealm/", "baked.txt", "Lorem ipsum dolor sit amet"))
    run(service.create(request)).shouldEqual(Left(NotAuthorizedError()))
  }

  it should "return an existing file" in new VipCacheOkMocks {
    val file = FileModel(
      id = Random.alphanumeric.take(5).mkString,
      path = "/PotatoRealm/",
      name = "baked.txt",
      createdAt = LocalDateTime.now,
      createdBy = "potato",
      content = "Lorem ipsum dolor sit amet"
    )

    repo.get(file.id).returns(EitherT.rightT[IO, AppError](Some(file)))

    run(service.get(GetFileRequest("potato", file.id))).shouldEqual(Right(file))
  }

  it should "not return a non-existing file" in new VipCacheOkMocks {
    val id = Random.alphanumeric.take(5).mkString

    repo.get(id).returns(EitherT.rightT[IO, AppError](None))

    run(service.get(GetFileRequest("potato", id))).shouldEqual(Left(NotFoundError()))
  }

  it should "list files in a directory" in new VipCacheOkMocks {
    val files = Seq(
      DirectoryItem(Random.alphanumeric.take(5).mkString, "baked.txt"),
      DirectoryItem(Random.alphanumeric.take(5).mkString, "fried.txt")
    )
    repo.getDir("/PotatoRealm/").returns(EitherT.rightT[IO, AppError](files))

    run(service.getDir(GetFileRequest("potato", "/PotatoRealm/"))).shouldEqual(Right(Directory(files)))
  }

  it should "not list files in an empty directory" in new VipCacheOkMocks {
    repo.getDir("/PotatoRealm/").returns(EitherT.rightT[IO, AppError](Seq.empty))

    run(service.getDir(GetFileRequest("potato", "/PotatoRealm/"))).shouldEqual(Left(NotFoundError()))
  }

  trait VipCacheOkMocks {
    vc.canAccessPath("potato", "/PotatoRealm/").returns(true)
    vc.canAccessFile("potato", "baked.txt").returns(true)
  }
}
