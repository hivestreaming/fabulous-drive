package hive.service

sealed trait AppError

case class DbError(message: String) extends AppError

case class NotFoundError(message: String = "Not found") extends AppError

case class NotAuthorizedError(message: String = "Not authorized") extends AppError
