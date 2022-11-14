package hive.service.files

import scala.util.Random

object IdGenerator {

  def next(): String = {
    Random.alphanumeric.take(6).mkString
  }

}
