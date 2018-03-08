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
      buildPretext(projectKey, content.getKeyId, activity.getCreatedUser.getName, activity.getCreated, "イシューを追加", Some(List(s"優先度: ${issue.getPriority.getName}", s"担当者: ${issue.getAssignee.getName}"))),
      buildTitle(content.getSummary),
      buildLink(spaceId, projectKey, content.getKeyId, commentId = None),
      createText(content.getDescription)
    )
  }

  def build(spaceId: String, projectKey: String, activity: Activity, content: IssueUpdatedContent, comment: IssueComment): Message = {
    val changeLogMessage = comment.getChangeLog.asScala.toList.filter(_.getField != "description").map(change => s"${change.getField}: ${change.getOriginalValue} -> ${change.getNewValue}")
    Message(
      buildPretext(projectKey, content.getKeyId, comment.getCreatedUser.getName, comment.getCreated, "イシューを更新", Some(changeLogMessage)),
      buildTitle(content.getSummary),
      buildLink(spaceId, projectKey, content.getKeyId, Some(comment.getId)),
      updateText(content.getComment.getContent, comment.getChangeLog.asScala.toList)
    )
  }

  def build(spaceId: String, projectKey: String, activity: Activity, content: IssueCommentedContent, comment: IssueComment): Message = {
    Message(
      buildPretext(projectKey, content.getKeyId, comment.getCreatedUser.getName, comment.getCreated, "コメントを追加", None),
      buildTitle(content.getSummary),
      buildLink(spaceId, projectKey, content.getKeyId, Some(comment.getId)),
      commentText(content.getComment.getContent)
    )
  }

  def buildPretext(projectKey: String, issueId: Long, updatedUser: String, createdAt: java.util.Date, operation: String, metaInformation: Option[List[String]]): String =
    s"""========================================
       |:memo: 【$operation】
       |対象イシュー: $projectKey-$issueId
       |更新者: $updatedUser
       |更新日: ${"%tF %<tT" format createdAt}${if (metaInformation.isDefined) "\n" + metaInformation.mkString("\n")}""".stripMargin

  def buildTitle(title: String): String = title

  def buildLink(spaceId: String, projectKey: String, issueId: Long, commentId: Option[Long]): String = s"https://$spaceId.backlog.jp/view/$projectKey-$issueId${if (commentId.isDefined) s"#comment-$commentId"}"

  def createText(content: String): String = packText(content, 1000)

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

  def commentText(content: String): String = packText(content, 1000)

  def packText(raw: String, maxLength: Int): String = if (raw.length <= maxLength) raw else raw.take(maxLength - 3) + "..."
}
