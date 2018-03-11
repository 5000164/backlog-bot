package jp._5000164.backlog_bot.interfaces

import java.util.Date

object Application extends App {
  val backlog = new Backlog
  val slack = new Slack

  val lastExecutedAt = Recorder.getLastExecutedAt
  val messageBundles = backlog.fetchMessages(lastExecutedAt)
  slack.post(messageBundles)
  Recorder.record(new Date)

  slack.system.terminate
}
