package jp._5000164.backlog_bot.domain

import com.nulabinc.backlog4j.ChangeLog

object BuildMessage {
  def createPretext(projectKey: String, issueId: Long, updatedUser: String, createdAt: java.util.Date, priority: String, assignee: String): String = {
    s"""========================================
       |:memo: 【イシューを追加】
       |更新者: $updatedUser
       |更新日: ${"%tF %<tT" format createdAt}
       |優先度: $priority
       |担当者: $assignee""".stripMargin
  }

  def createTitle(title: String): String = title

  def createLink(spaceId: String, projectKey: String, issueId: Long): String = {
    s"https://$spaceId.backlog.jp/view/$projectKey-$issueId"
  }

  def createText(content: String): String = {
    if (content.length <= 1000) content else content.take(997) + "..."
  }

  def updatePretext(projectKey: String, issueId: Long, updatedUser: String, createdAt: java.util.Date, changes: List[ChangeLog]): String = {
    val changeLogMessage = changes.map(change =>
      if (change.getField == "description") s"description 追加: ${change.getNewValue diff change.getOriginalValue}\ndescription 削除: ${change.getOriginalValue diff change.getNewValue}"
      else s"${change.getField}: ${change.getOriginalValue} -> ${change.getNewValue}"
    ).mkString("\n")
    s"""========================================
       |:memo: 【イシューを更新】
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
    if (content.length <= 1000) content else content.take(997) + "..."
  }

  def commentPretext(projectKey: String, issueId: Long, updatedUser: String, createdAt: java.util.Date): String = {
    s"""========================================
       |:memo: 【コメントを追加】
       |対象イシュー: $projectKey-$issueId
       |更新者: $updatedUser
       |更新日: ${"%tF %<tT" format createdAt}""".stripMargin
  }

  def commentTitle(title: String): String = title

  def commentLink(spaceId: String, projectKey: String, issueId: Long, commentId: Long): String = {
    s"https://$spaceId.backlog.jp/view/$projectKey-$issueId#comment-$commentId"
  }

  def commentText(content: String): String = {
    if (content.length <= 1000) content else content.take(997) + "..."
  }
}
