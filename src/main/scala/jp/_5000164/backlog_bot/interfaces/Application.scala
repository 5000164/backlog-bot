package jp._5000164.backlog_bot.interfaces

import java.util.Date

import jp._5000164.backlog_bot.domain.Recorder
import jp._5000164.backlog_bot.infractructure.Settings

object Application extends App {
  val executedAt = new Date

  val backlog = new Backlog
  val slack = new Slack
  val reader = new Reader
  val writer = new Writer

  val lastExecutedAt = Recorder.getLastExecutedAt(reader)
  val mapping = Settings.settings.mapping
  val messageBundles = backlog.fetchMessages(lastExecutedAt, mapping)
  slack.post(messageBundles)
  Recorder.record(executedAt, writer)

  slack.system.terminate
}
