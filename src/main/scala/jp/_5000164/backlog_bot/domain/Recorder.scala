package jp._5000164.backlog_bot.domain

import java.text.SimpleDateFormat
import java.util.Date

import jp._5000164.backlog_bot.interfaces.{Reader, Writer}

object Recorder {
  def getLastExecutedAt(reader: Reader): Date =
    reader.readRecord() match {
      case Some(content) => new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(content)
      case None => new Date
    }

  def record(executedAt: Date, writer: Writer): Unit =
    writer.writeRecord(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(executedAt))
}
