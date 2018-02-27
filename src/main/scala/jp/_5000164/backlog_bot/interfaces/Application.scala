package jp._5000164.backlog_bot.interfaces

import java.util.Date

object Application extends App {
  val backlog = new Backlog
  val slack = new Slack

  val lastExec = Recorder.getLastExec
  val messages = backlog.fetchMessages(lastExec)
  slack.post(messages)
  Recorder.record(new Date)

  slack.system.terminate
}
