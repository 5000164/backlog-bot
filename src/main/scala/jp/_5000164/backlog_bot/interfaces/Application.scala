package jp._5000164.backlog_bot.interfaces

object Application extends App {
  val backlog = new Backlog
  val slack = new Slack

  val lastExec = Recorder.getLastExec
  val messages = backlog.fetchMessages(lastExec)
  slack.post(messages)

  slack.system.terminate
}
