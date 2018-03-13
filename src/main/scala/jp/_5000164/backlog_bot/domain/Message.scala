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
    val metaInformation = List(s"優先度: ${issue.getPriority.getName}", s"担当者: ${issue.getAssignee.getName}")
    Message(
      buildPretext(projectKey, content.getKeyId, activity.getCreatedUser.getName, activity.getCreated, "イシューを追加", Some(metaInformation)),
      buildTitle(content.getSummary),
      buildLink(spaceId, projectKey, content.getKeyId, commentId = None),
      buildText(content.getDescription, 1000)
    )
  }

  def build(spaceId: String, projectKey: String, activity: Activity, content: IssueUpdatedContent, comment: IssueComment): Message = {
    val changeLogMessage = comment.getChangeLog.asScala.toList.filter(_.getField != "description").map(change => s"${change.getField}: ${change.getOriginalValue} -> ${change.getNewValue}")
    val changes = comment.getChangeLog.asScala.toList
    val descriptionChange = changes.find(_.getField == "description")
    val text = if (descriptionChange.isDefined) {
      val addDescription = calculateDiff(descriptionChange.get.getNewValue, descriptionChange.get.getOriginalValue, 300)
      val removeDescription = calculateDiff(descriptionChange.get.getOriginalValue, descriptionChange.get.getNewValue, 300)
      s"""```
         |+ $addDescription
         |```
         |```
         |- $removeDescription
         |```
         |
         |${content.getComment.getContent}""".stripMargin
    } else {
      content.getComment.getContent
    }
    Message(
      buildPretext(projectKey, content.getKeyId, comment.getCreatedUser.getName, comment.getCreated, "イシューを更新", Some(changeLogMessage)),
      buildTitle(content.getSummary),
      buildLink(spaceId, projectKey, content.getKeyId, Some(comment.getId)),
      buildText(text, 1000)
    )
  }

  def build(spaceId: String, projectKey: String, activity: Activity, content: IssueCommentedContent, comment: IssueComment): Message = {
    Message(
      buildPretext(projectKey, content.getKeyId, comment.getCreatedUser.getName, comment.getCreated, "コメントを追加", None),
      buildTitle(content.getSummary),
      buildLink(spaceId, projectKey, content.getKeyId, Some(comment.getId)),
      buildText(comment.getContent, 1000)
    )
  }

  def buildPretext(projectKey: String, issueId: Long, updatedUser: String, createdAt: java.util.Date, operation: String, metaInformation: Option[List[String]]): String =
    s"""========================================
       |:memo: 【$operation】
       |対象イシュー: $projectKey-$issueId
       |更新者: $updatedUser
       |更新日: ${"%tF %<tT" format createdAt}${if (metaInformation.isDefined) "\n" + metaInformation.get.mkString("\n") else ""}""".stripMargin

  def buildTitle(title: String): String = title

  def buildLink(spaceId: String, projectKey: String, issueId: Long, commentId: Option[Long]): String =
    s"https://$spaceId.backlog.jp/view/$projectKey-$issueId${if (commentId.isDefined) s"#comment-${commentId.get}" else ""}"

  def buildText(content: String, maxLength: Int): String = packText(content, maxLength)

  def packText(raw: String, maxLength: Int): String = if (raw.length <= maxLength) raw else raw.take(maxLength - 3) + "..."

  def calculateDiff(before: String, after: String, maxLength: Int): String = packText(before diff after, maxLength)
}
