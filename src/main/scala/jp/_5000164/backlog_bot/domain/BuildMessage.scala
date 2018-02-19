package jp._5000164.backlog_bot.domain

object BuildMessage {
  def commentTitle(title: String): String = title

  def commentLink(spaceId: String, projectKey: String, issueId: Long, commentId: Long): String = {
    s"https://$spaceId.backlog.jp/view/$projectKey-$issueId#comment-$commentId"
  }

  def commentText(content: String, updatedUser: String): String = {
    s"${content.take(7900)}\n\nupdated by $updatedUser"
  }
}
