package jp._5000164.backlog_bot.interfaces

import java.io.PrintWriter
import java.nio.file.{Files, Paths}

class Writer {
  def writeRecord(content: String): Unit = {
    if (Files.notExists(Paths.get(".record"))) Files.createFile(Paths.get(".record"))
    val pw = new PrintWriter(".record")
    pw.write(content)
    pw.close()
  }
}
