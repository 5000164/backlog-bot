package jp._5000164.backlog_bot.interfaces

import java.util.Date

import jp._5000164.backlog_bot.infractructure.Settings

object Application extends App {
  val backlog = new Backlog
  val slack = new Slack

  val lastExecutedAt = Recorder.getLastExecutedAt
  val mapping = Settings.settings.mapping
  val messageBundles = backlog.fetchMessages(lastExecutedAt, mapping)
  slack.post(messageBundles)
  Recorder.record(new Date)

  slack.system.terminate
}
