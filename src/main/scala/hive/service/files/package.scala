package hive.service

import java.time.LocalDateTime

package object files {

  case class FileModel(
    id: String,
    path: String,
    name: String,
    createdAt: LocalDateTime,
    createdBy: String,
    content: String
  )

  case class DirectoryItem(id: String, name: String)

  case class Directory(items: Seq[DirectoryItem])

  case class CreateFilePayload(
    path: String,
    name: String,
    content: String
  )

  case class CreateFileRequest(user: String, payload: CreateFilePayload)

  case class GetFileRequest(user: String, path: String)

}
