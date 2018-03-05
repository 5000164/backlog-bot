package jp._5000164.backlog_bot.domain

import com.nulabinc.backlog4j._
import com.nulabinc.backlog4j.internal.json.activities.{IssueCommentedContent, IssueCreatedContent, IssueUpdatedContent}

import scala.collection.JavaConverters._

case class Message(
                    pretext: String,
                    title: String,
                    link: String,
                    content: String
                  )

object Message {
  def build(spaceId: String, projectKey: String, activity: Activity, content: IssueCreatedContent, issue: Issue): Message = {
    Message(
      createPretext(projectKey, content.getKeyId, activity.getCreatedUser.getName, activity.getCreated, issue.getPriority.getName, issue.getAssignee.getName),
      createTitle(content.getSummary),
      createLink(spaceId, projectKey, content.getKeyId),
      createText(content.getDescription)
    )
  }

  def build(spaceId: String, projectKey: String, activity: Activity, content: IssueUpdatedContent, comment: IssueComment): Message = {
    Message(
      updatePretext(projectKey, content.getKeyId, comment.getCreatedUser.getName, comment.getCreated, comment.getChangeLog.asScala.toList),
      updateTitle(content.getSummary),
      updateLink(spaceId, projectKey, content.getKeyId, comment.getId),
      updateText(content.getComment.getContent, comment.getChangeLog.asScala.toList)
    )
  }

  def build(spaceId: String, projectKey: String, activity: Activity, content: IssueCommentedContent, comment: IssueComment): Message = {
    Message(
      commentPretext(projectKey, content.getKeyId, comment.getCreatedUser.getName, comment.getCreated),
      commentTitle(content.getSummary),
      commentLink(spaceId, projectKey, content.getKeyId, comment.getId),
      commentText(content.getComment.getContent)
    )
  }

  def createPretext(projectKey: String, issueId: Long, updatedUser: String, createdAt: java.util.Date, priority: String, assignee: String): String = {
    s"""========================================
       |:memo: 【イシューを追加】
       |対象イシュー: $projectKey-$issueId
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
    val changeLogMessage = changes.filter(_.getField != "description").map(change => s"${change.getField}: ${change.getOriginalValue} -> ${change.getNewValue}").mkString("\n")
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

  def updateText(content: String, changes: List[ChangeLog]): String = {
    val descriptionChange = changes.find(_.getField == "description")
    if (descriptionChange.isDefined) {
      val addDescription = descriptionChange.get.getNewValue diff descriptionChange.get.getOriginalValue
      val removeDescription = descriptionChange.get.getOriginalValue diff descriptionChange.get.getNewValue
      s"""description 追加: ${if (addDescription.length <= 300) addDescription else addDescription.take(297) + "..."}
         |description 削除: ${if (removeDescription.length <= 300) removeDescription else removeDescription.take(297) + "..."}
         |コメント: ${if (content.length <= 400) content else content.take(397) + "..."}""".stripMargin
    } else {
      if (content.length <= 1000) content else content.take(997) + "..."
    }
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
