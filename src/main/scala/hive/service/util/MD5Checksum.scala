package hive.service.util

import java.security.MessageDigest

object MD5Checksum {
  private def toHex(bytes: Array[Byte]): String = {
    val sb = new StringBuilder()
    bytes.foreach(b => sb.append(Integer.toHexString((b.toInt & 0xFF) | 0x100).substring(1, 3)))
    sb.toString()
  }

  def apply(data: String): String = {
    val md = MessageDigest.getInstance("MD5")
    toHex(md.digest(data.getBytes))
  }
}
