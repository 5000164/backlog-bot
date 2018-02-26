package jp._5000164.backlog_bot.interfaces

import java.text.SimpleDateFormat
import java.util.Date

object Recorder {
  def getLastExec: Date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2018-02-26 00:00:00")
}
