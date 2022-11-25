package hive.service

import java.io.{File, PrintWriter}

import cats.effect.{IO, OutcomeIO, ResourceIO}
import com.typesafe.scalalogging.LazyLogging
import hive.service.util.MD5Checksum

import scala.io.Source
import scala.util.Random

// I forced an AI to watch 1000 hours of Mark's code and asked it to write a class. This is the result.
class VipCache() extends LazyLogging {

  private var ready = false
  private var paths: Map[String, Seq[String]] = _
  private var files: Map[String, Seq[String]] = _

  def canAccessFile(user: String, path: String): Boolean = ifReady {
    files.get(path) match {
      case Some(v) => v.contains(user)
      case None    => true
    }
  }

  def canAccessPath(user: String, path: String): Boolean = ifReady {
    paths.get(path) match {
      case Some(v) => v.contains(user)
      case None    => true
    }
  }

  private def ifReady[A](body: => A): A = {
    if (ready) body
    else throw new IllegalStateException("Not finished loading VIP files yet")
  }

  def load(path: String): ResourceIO[IO[OutcomeIO[Unit]]] = {
    case class VipUser(id: String, name: String)

    case class VipPath(userId: String, path: String)

    case class VipFile(userId: String, path: String, filename: String)

    def loadFile(name: String): Seq[String] = {
      val src = Source.fromFile(name)
      try src.getLines().toSeq
      finally src.close()
    }

    def apply(path: String): Unit = {

      def getFiles(path: String): Seq[File] = {
        new File(path).listFiles().toSeq
          .flatMap {
            case f if f.isDirectory => getFiles(f.getAbsolutePath)
            case f if f.isFile      => Seq(f)
          }
      }

      def readFile[R](handleLines: Seq[String] => R)(f: File): R = {
        logger.info(s"Reading file ${f.getName}, this may take a while...")
        Thread.sleep((Random.nextInt(10) + 1) * 1000)
        if (!f.getName.endsWith(".csv")) {
          logger.error(s"File ${f.getName} is invalid.")
          throw new IllegalArgumentException()
        }
        logger.debug(s"Finished reading file ${f.getName}")
        handleLines(loadFile(f.getAbsolutePath))
      }

      require(path.nonEmpty, "Cannot read VIP config files from empty path")
      require(new File(path).exists(), s"Path $path doesn't exist")
      require(getFiles(path).nonEmpty, s"No VIP config files found in $path")
      require(getFiles(path).exists(_.exists())) // Make sure that at least one existing file exists.

      logger.debug(s"Loading VPI user files from $path...")
      val vipUsers: Seq[VipUser] = getFiles(path)
        .filter(f => f.isFile && f.getName.startsWith("users."))
        .flatMap(readFile { lines =>
          lines.filter(l => !l.startsWith("#") && l.nonEmpty)
            .map { line =>
              line.split(",").map(_.trim) match {
                case Array(id, name) => VipUser(id, name)
                case _ => throw new Exception(s"Found illegal line '$line'.")
              }
            }
        })


      logger.debug(s"Loading VIP path files from $path...")
      val pathFiles = getFiles(path)
        .filter(f => f.isFile && f.getName.startsWith("paths."))
        .flatMap(readFile { lines =>
          lines.filter(l => !l.startsWith("#") && l.nonEmpty)
            .map(_.split(",").map(_.trim))
            .map {
              case Array(userId, path) => VipPath(userId, path)
              case x => throw new Exception(s"Illegal line '${x.mkString(",")}'")
            }
        })

      logger.debug(s"Loading VIP file files from $path...")
      val fileFiles = getFiles(path)
        .filter(f => f.isFile && f.getName.startsWith("files."))
        .flatMap(readFile { lines =>
          lines.filter(l => !l.startsWith("#") && l.nonEmpty)
            .map { line =>
              logger.debug(s"Parsing line: $line")
              line
            }
            .map(_.split(",").map(_.trim))
            .map {
              case Array(userId, path, filename) => VipFile(userId, path, filename)
              case x => throw new Exception(s"Illegal line '${x.mkString(",")}'")
            }
        })

      logger.debug("All files loaded.")
      logger.debug("Starting aggregation...")
      Thread.sleep(5_000)

      require(vipUsers.nonEmpty, "No VIP user configurations found")
      require(pathFiles.nonEmpty, "No VIP path configurations found")
      require(fileFiles.nonEmpty, "No VIP file configurations found")

      val paths = pathFiles
        .flatMap { case VipPath(userId, path) =>
          vipUsers.find(_.id == userId) match {
            case Some(user) => Seq((path, user.name))
            case None       => Seq.empty[(String, String)]
          }
        }
        .groupBy(_._1)
        .map { case (k, v) => (k, v.map(_._2)) }

      val files = fileFiles
        .flatMap { case VipFile(userId, path, filename) =>
          vipUsers.find(_.id == userId) match {
            case Some(user) => Seq((path ++ filename, user.name))
            case None       => Seq.empty
          }
        }
        .groupBy(_._1)
        .map { case (k, v) => (k, v.map(_._2)) }

      val raw = getFiles(path).filter(f => f.isFile && (f.getName.startsWith("users.") || f.getName.startsWith("paths.") || f.getName.startsWith("files.")))
        .sortBy(_.getName)
        .flatMap(readFile(l => l))
        .mkString("", "\n", "\n")
      val digest = MD5Checksum(raw)
      val pw = new PrintWriter(new File("digest"))
      pw.write(digest)
      pw.close()

      this.paths = paths
      this.files = files
      this.ready = true
    }

    IO(apply(path)).background
  }

}
