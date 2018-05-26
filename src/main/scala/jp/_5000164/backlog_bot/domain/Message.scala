package jp._5000164.backlog_bot.domain

import com.nulabinc.backlog4j._
import com.nulabinc.backlog4j.internal.json.activities._

import scala.collection.JavaConverters._

case class Message(
    authorName: Option[String],
    pretext: Option[String],
    title: Option[String],
    link: Option[String],
    text: Option[String]
)

object Message {
  val maxLength = 500

  def build(spaceId: String, projectKey: String, activity: Activity, content: IssueCreatedContent, issue: Issue): Message = {
    val metaInformation = List(
      if (issue.getPriority != null) Some(s"優先度: ${issue.getPriority.getName}") else None,
      if (issue.getAssignee != null) Some(s"担当者: ${issue.getAssignee.getName}") else None
    )
    Message(
      buildAuthorName(activity.getCreatedUser.getName),
      buildPretext(s":heavy_plus_sign: イシュー $projectKey-${content.getKeyId} を追加", metaInformation),
      buildTitle(content.getSummary),
      buildIssueLink(spaceId, projectKey, content.getKeyId, commentId = None),
      buildText(content.getDescription, maxLength)
    )
  }

  def build(spaceId: String, projectKey: String, activity: Activity, content: IssueUpdatedContent, comment: IssueComment): Message = {
    val changes = comment.getChangeLog.asScala.toList
    val changeLogMessage = changes.filter(_.getField != "description").map(change => Some(s"${change.getField}: ${change.getOriginalValue} -> ${change.getNewValue}"))
    val descriptionChange = changes.find(_.getField == "description")
    val text = if (descriptionChange.isDefined) {
      val addDescription = calculateDiff(descriptionChange.get.getNewValue, descriptionChange.get.getOriginalValue, maxLength / 2 - 20)
      val removeDescription = calculateDiff(descriptionChange.get.getOriginalValue, descriptionChange.get.getNewValue, maxLength / 2 - 20)
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
      buildAuthorName(comment.getCreatedUser.getName),
      buildPretext(s":arrows_counterclockwise: イシュー $projectKey-${content.getKeyId} を更新", changeLogMessage),
      buildTitle(content.getSummary),
      buildIssueLink(spaceId, projectKey, content.getKeyId, Some(comment.getId)),
      buildText(text, maxLength)
    )
  }

  def build(spaceId: String, projectKey: String, activity: Activity, content: IssueCommentedContent, comment: IssueComment): Message =
    Message(
      buildAuthorName(comment.getCreatedUser.getName),
      buildPretext(s":speech_balloon: イシュー $projectKey-${content.getKeyId} にコメントを追加", metaInformation = List()),
      buildTitle(content.getSummary),
      buildIssueLink(spaceId, projectKey, content.getKeyId, Some(comment.getId)),
      buildText(Option(comment.getContent).getOrElse(""), maxLength)
    )

  def build(spaceId: String, projectKey: String, activity: Activity, content: WikiCreatedContent): Message =
    Message(
      buildAuthorName(activity.getCreatedUser.getName),
      buildPretext(s":heavy_plus_sign: Wiki を追加", metaInformation = List()),
      buildTitle(content.getName),
      buildWikiLink(spaceId, projectKey, content.getName, version = None),
      buildText(Option(content.getContent).getOrElse(""), maxLength)
    )

  def build(spaceId: String, projectKey: String, activity: Activity, content: WikiUpdatedContent): Message =
    Message(
      buildAuthorName(activity.getCreatedUser.getName),
      buildPretext(s":arrows_counterclockwise: Wiki を更新", metaInformation = List()),
      buildTitle(content.getName),
      buildWikiLink(spaceId, projectKey, content.getName, Some(content.getVersion)),
      buildText(Option(content.getDiff).getOrElse(""), maxLength)
    )

  def build(spaceId: String, projectKey: String, activity: Activity, content: GitPushedContent): Message = {
    val branchName = content.getRef.drop(11)
    val commitLog = content.getRevisions.asScala.map(_.getComment).mkString("\n")

    Message(
      buildAuthorName(activity.getCreatedUser.getName),
      buildPretext(":git: git push", metaInformation = List()),
      buildTitle(s"${content.getRepository.getName}/$branchName"),
      buildBranchLink(spaceId, projectKey, content.getRepository.getName, branchName),
      buildText(commitLog, maxLength)
    )
  }

  def build(spaceId: String, projectKey: String, activity: Activity, content: PullRequestContent, pullRequest: PullRequest): Message =
    Message(
      buildAuthorName(pullRequest.getCreatedUser.getName),
      buildPretext(s":heavy_plus_sign: プルリクエスト ${content.getRepository.getName}/${content.getNumber} を追加", metaInformation = List()),
      buildTitle(pullRequest.getSummary),
      buildPullRequestLink(spaceId, projectKey, content.getRepository.getName, content.getNumber, commentId = None),
      buildText(Option(pullRequest.getDescription).getOrElse(""), maxLength)
    )

  def build(spaceId: String, projectKey: String, activity: Activity, content: PullRequestContent, comment: PullRequestComment): Message = {
    val changes = comment.getChangeLog.asScala.toList
    val changeLogMessage = changes.filter(_.getField != "description").map(change => Some(s"${change.getField}: ${change.getOriginalValue} -> ${change.getNewValue}"))
    Message(
      buildAuthorName(comment.getCreatedUser.getName),
      buildPretext(s":speech_balloon: プルリクエスト ${content.getRepository.getName}/${content.getNumber} にコメントを追加", changeLogMessage),
      buildTitle(content.getSummary),
      buildPullRequestLink(spaceId, projectKey, content.getRepository.getName, content.getNumber, Some(comment.getId)),
      buildText(Option(comment.getContent).getOrElse(""), maxLength)
    )
  }

  def buildAuthorName(updatedUser: String): Option[String] = Some(updatedUser)

  def buildPretext(operation: String, metaInformation: List[Option[String]]): Option[String] = {
    val flatInformation = metaInformation.flatten
    Some( s"""========================================
             |$operation""".stripMargin + (if (flatInformation.nonEmpty) "\n" + flatInformation.mkString("\n") else ""))
  }

  def buildTitle(title: String): Option[String] = Some(title)

  def buildIssueLink(spaceId: String, projectKey: String, issueId: Long, commentId: Option[Long]): Option[String] =
    Some(s"https://$spaceId.backlog.jp/view/$projectKey-$issueId${if (commentId.isDefined) s"#comment-${commentId.get}" else ""}")

  def buildWikiLink(spaceId: String, projectKey: String, name: String, version: Option[Int]): Option[String] =
    Some(s"https://$spaceId.backlog.jp/wiki/$projectKey/$name${if (version.isDefined) s"/diff/${version.get - 1}...${version.get}" else ""}")

  def buildBranchLink(spaceId: String, projectKey: String, repository: String, branch: String): Option[String] =
    Some(s"https://$spaceId.backlog.jp/git/$projectKey/$repository/history/$branch")

  def buildPullRequestLink(spaceId: String, projectKey: String, repository: String, number: Long, commentId: Option[Long]): Option[String] =
    Some(s"https://$spaceId.backlog.jp/git/$projectKey/$repository/pullRequests/$number${if (commentId.isDefined) s"#comment-${commentId.get}" else ""}")

  def buildText(content: String, maxLength: Int): Option[String] = Some(packText(content, maxLength))

  def packText(raw: String, maxLength: Int): String = if (raw.length <= maxLength) raw else raw.take(maxLength - 3) + "..."

  def calculateDiff(before: String, after: String, maxLength: Int): String = packText(before diff after, maxLength)
}
