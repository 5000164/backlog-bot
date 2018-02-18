package jp._5000164.backlog_bot.interfaces

object Application extends App {
  val backlog = new Backlog
  val slack = new Slack

  val message = backlog.fetchComment
  slack.post(message)
}
