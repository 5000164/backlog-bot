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
      buildTitle(content.getSummary),
      buildLink(spaceId, projectKey, content.getKeyId, commentId = None),
      createText(content.getDescription)
    )
  }

  def build(spaceId: String, projectKey: String, activity: Activity, content: IssueUpdatedContent, comment: IssueComment): Message = {
    Message(
      updatePretext(projectKey, content.getKeyId, comment.getCreatedUser.getName, comment.getCreated, comment.getChangeLog.asScala.toList),
      buildTitle(content.getSummary),
      buildLink(spaceId, projectKey, content.getKeyId, Some(comment.getId)),
      updateText(content.getComment.getContent, comment.getChangeLog.asScala.toList)
    )
  }

  def build(spaceId: String, projectKey: String, activity: Activity, content: IssueCommentedContent, comment: IssueComment): Message = {
    Message(
      commentPretext(projectKey, content.getKeyId, comment.getCreatedUser.getName, comment.getCreated),
      buildTitle(content.getSummary),
      buildLink(spaceId, projectKey, content.getKeyId, Some(comment.getId)),
      commentText(content.getComment.getContent)
    )
  }

  def buildTitle(title: String): String = title

  def buildLink(spaceId: String, projectKey: String, issueId: Long, commentId: Option[Long]): String = {
    s"https://$spaceId.backlog.jp/view/$projectKey-$issueId${if (commentId.isDefined) s"#comment-$commentId"}"
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

  def createText(content: String): String = packText(content, 1000)

  def updatePretext(projectKey: String, issueId: Long, updatedUser: String, createdAt: java.util.Date, changes: List[ChangeLog]): String = {
    val changeLogMessage = changes.filter(_.getField != "description").map(change => s"${change.getField}: ${change.getOriginalValue} -> ${change.getNewValue}").mkString("\n")
    s"""========================================
       |:memo: 【イシューを更新】
       |対象イシュー: $projectKey-$issueId
       |更新者: $updatedUser
       |更新日: ${"%tF %<tT" format createdAt}
       |$changeLogMessage""".stripMargin
  }

  def updateText(content: String, changes: List[ChangeLog]): String = {
    val descriptionChange = changes.find(_.getField == "description")
    if (descriptionChange.isDefined) {
      val addDescription = descriptionChange.get.getNewValue diff descriptionChange.get.getOriginalValue
      val removeDescription = descriptionChange.get.getOriginalValue diff descriptionChange.get.getNewValue
      s"""description 追加: ${packText(addDescription, 300)}
         |description 削除: ${packText(removeDescription, 300)}
         |コメント: ${packText(content, 400)}""".stripMargin
    } else {
      packText(content, 1000)
    }
  }

  def commentPretext(projectKey: String, issueId: Long, updatedUser: String, createdAt: java.util.Date): String = {
    s"""========================================
       |:memo: 【コメントを追加】
       |対象イシュー: $projectKey-$issueId
       |更新者: $updatedUser
       |更新日: ${"%tF %<tT" format createdAt}""".stripMargin
  }

  def commentText(content: String): String = packText(content, 1000)

  def packText(raw: String, maxLength: Int): String = if (raw.length <= maxLength) raw else raw.take(maxLength - 3) + "..."
}
