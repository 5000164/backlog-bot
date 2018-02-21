package jp._5000164.backlog_bot.domain

object BuildMessage {
  def updatePretext(projectKey: String, issueId: Long, updatedUser: String, createdAt: java.util.Date): String = {
    s"""イシューを更新
       |対象イシュー: [$projectKey-$issueId]
       |更新者: $updatedUser
       |更新日: ${"%tF %<tT" format createdAt}""".stripMargin
  }

  def updateTitle(title: String): String = title

  def updateLink(spaceId: String, projectKey: String, issueId: Long, commentId: Long): String = {
    s"https://$spaceId.backlog.jp/view/$projectKey-$issueId#comment-$commentId"
  }

  def updateText(content: String): String = {
    if (content.length <= 8000) content else content.take(7997) + "..."
  }

  def commentTitle(title: String, projectKey: String, issueId: Long, updatedUser: String, createdAt: java.util.Date): String = {
    s"[$projectKey-$issueId] $title へコメント追加 $updatedUser ${"%tF %<tT" format createdAt}"
  }

  def commentLink(spaceId: String, projectKey: String, issueId: Long, commentId: Long): String = {
    s"https://$spaceId.backlog.jp/view/$projectKey-$issueId#comment-$commentId"
  }

  def commentText(content: String): String = {
    if (content.length <= 8000) content else content.take(7997) + "..."
  }
}
