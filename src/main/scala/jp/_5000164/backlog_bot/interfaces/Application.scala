package jp._5000164.backlog_bot.interfaces

import java.text.SimpleDateFormat
import java.util.Date

import jp._5000164.backlog_bot.domain.Recorder
import jp._5000164.backlog_bot.infrastructure.Settings

object Application extends App {
  val executedAt = new Date

  val keyArgs = args.collect {
    case "--dry-run" => "dry-run"
  }.toSet
  val keyValueArgs = args.sliding(2).toList.collect {
    case Array("--date", specifiedDate: String) => "date" -> Some(specifiedDate)
  }.toMap

  val backlog = new Backlog
  val slack = new Slack
  val reader = new Reader
  val writer = new Writer

  val lastExecutedAt = keyValueArgs.getOrElse("date", None).map(date => new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date)).getOrElse(Recorder.getLastExecutedAt(reader))
  val projects = Settings.settings.projects
  val messageBundles = backlog.fetchMessages(lastExecutedAt, projects)

  if (!keyArgs.contains("dry-run")) {
    slack.post(messageBundles)
    Recorder.record(executedAt, writer)
    slack.log(Settings.settings.logChannelId, messageBundles.length)
  }

  slack.system.terminate
}
