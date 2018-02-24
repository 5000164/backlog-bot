package jp._5000164.backlog_bot.domain

import com.nulabinc.backlog4j.ChangeLog

object BuildMessage {
  def updatePretext(projectKey: String, issueId: Long, updatedUser: String, createdAt: java.util.Date, changes: List[ChangeLog]): String = {
    val changeLogMessage = changes.map(change => s"${change.getField}: ${change.getOriginalValue} -> ${change.getNewValue}").mkString("\n")
    s"""イシューを更新
       |対象イシュー: $projectKey-$issueId
       |更新者: $updatedUser
       |更新日: ${"%tF %<tT" format createdAt}
       |$changeLogMessage""".stripMargin
  }

  def updateTitle(title: String): String = title

  def updateLink(spaceId: String, projectKey: String, issueId: Long, commentId: Long): String = {
    s"https://$spaceId.backlog.jp/view/$projectKey-$issueId#comment-$commentId"
  }

  def updateText(content: String): String = {
    if (content.length <= 4000) content else content.take(3997) + "..."
  }

  def commentPretext(projectKey: String, issueId: Long, updatedUser: String, createdAt: java.util.Date): String = {
    s"""コメントを追加
       |対象イシュー: $projectKey-$issueId
       |更新者: $updatedUser
       |更新日: ${"%tF %<tT" format createdAt}""".stripMargin
  }

  def commentTitle(title: String): String = title

  def commentLink(spaceId: String, projectKey: String, issueId: Long, commentId: Long): String = {
    s"https://$spaceId.backlog.jp/view/$projectKey-$issueId#comment-$commentId"
  }

  def commentText(content: String): String = {
    if (content.length <= 4000) content else content.take(3997) + "..."
  }
}
