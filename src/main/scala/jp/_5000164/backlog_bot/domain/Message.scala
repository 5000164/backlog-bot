package jp._5000164.backlog_bot.domain

import com.nulabinc.backlog4j._
import com.nulabinc.backlog4j.internal.json.RevisionJSONImpl
import com.nulabinc.backlog4j.internal.json.activities.{GitPushedContent, IssueCommentedContent, IssueCreatedContent, IssueUpdatedContent}

import scala.collection.JavaConverters._

case class Message(
                    pretext: Option[String],
                    title: Option[String],
                    link: Option[String],
                    content: Option[String]
                  )

object Message {
  def build(spaceId: String, projectKey: String, activity: Activity, content: IssueCreatedContent, issue: Issue): Message = {
    val metaInformation = List(s"優先度: ${issue.getPriority.getName}", s"担当者: ${issue.getAssignee.getName}")
    Message(
      buildPretext(activity.getCreatedUser.getName, activity.getCreated, s":heavy_plus_sign: $projectKey-${content.getKeyId} を追加", Some(metaInformation)),
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
         |${Option(comment.getContent).getOrElse("")}""".stripMargin
    } else {
      Option(comment.getContent).getOrElse("")
    }
    Message(
      buildPretext(comment.getCreatedUser.getName, comment.getCreated, s":arrows_counterclockwise: $projectKey-${content.getKeyId} を更新", Some(changeLogMessage)),
      buildTitle(content.getSummary),
      buildLink(spaceId, projectKey, content.getKeyId, Some(comment.getId)),
      buildText(text, 1000)
    )
  }

  def build(spaceId: String, projectKey: String, activity: Activity, content: IssueCommentedContent, comment: IssueComment): Message =
    Message(
      buildPretext(comment.getCreatedUser.getName, comment.getCreated, s":speech_balloon: $projectKey-${content.getKeyId} にコメントを追加", None),
      buildTitle(content.getSummary),
      buildLink(spaceId, projectKey, content.getKeyId, Some(comment.getId)),
      buildText(Option(comment.getContent).getOrElse(""), 1000)
    )

  def build(spaceId: String, projectKey: String, activity: Activity, content: GitPushedContent): Message = {
    val commits = content.getRevisions.toArray.toList.map(_.asInstanceOf[RevisionJSONImpl]).map(_.getComment)
    Message(
      buildPretext(activity.getCreatedUser.getName, activity.getCreated, s":arrow_up: ${content.getRepository.getName}/${content.getRef} へ push", None),
      None,
      None,
      Some(commits.mkString("\n"))
    )
  }

  def buildPretext(updatedUser: String, createdAt: java.util.Date, operation: String, metaInformation: Option[List[String]]): Option[String] =
    Some( s"""========================================
             |$operation
             |更新者: $updatedUser
             |更新日: ${"%tF %<tT" format createdAt}${if (metaInformation.isDefined) "\n" + metaInformation.get.mkString("\n") else ""}""".stripMargin)

  def buildTitle(title: String): Option[String] = Some(title)

  def buildLink(spaceId: String, projectKey: String, issueId: Long, commentId: Option[Long]): Option[String] =
    Some(s"https://$spaceId.backlog.jp/view/$projectKey-$issueId${if (commentId.isDefined) s"#comment-${commentId.get}" else ""}")

  def buildText(content: String, maxLength: Int): Option[String] = Some(packText(content, maxLength))

  def packText(raw: String, maxLength: Int): String = if (raw.length <= maxLength) raw else raw.take(maxLength - 3) + "..."

  def calculateDiff(before: String, after: String, maxLength: Int): String = packText(before diff after, maxLength)
}
