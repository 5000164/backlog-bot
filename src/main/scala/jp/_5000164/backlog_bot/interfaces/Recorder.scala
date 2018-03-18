package jp._5000164.backlog_bot.interfaces

import java.io.PrintWriter
import java.nio.file.{Files, Paths}
import java.text.SimpleDateFormat
import java.util.Date

import scala.io.Source

object Recorder {
  def getLastExecutedAt: Date =
    if (Files.notExists(Paths.get(".record"))) new Date
    else new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(Source.fromFile(".record").mkString)

  def record(executedAt: Date): Unit = {
    if (Files.notExists(Paths.get(".record"))) Files.createFile(Paths.get(".record"))
    val pw = new PrintWriter(".record")
    pw.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(executedAt))
    pw.close()
  }
}
