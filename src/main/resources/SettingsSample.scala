import jp._5000164.backlog_bot.infrastructure._

new SettingsType {
  val projects = Map(
    "PROJECT_KEY" -> Project(
      issue = Issue(postChannel = "post_channel"),
      wiki = Wiki(postChannel = "post_channel"),
      repositories = Map("RepositoryName" -> Repository(postChannel = "post_channel"))))
  val logChannelId = "log_channel_id"
}
