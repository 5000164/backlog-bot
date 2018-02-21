package jp._5000164.backlog_bot.interfaces

import com.nulabinc.backlog4j.conf.{BacklogConfigure, BacklogJpConfigure}
import com.nulabinc.backlog4j.internal.json.activities.{IssueCommentedContent, IssueUpdatedContent}
import com.nulabinc.backlog4j.{Activity, BacklogClient, BacklogClientFactory}
import jp._5000164.backlog_bot.domain.{BuildMessage, Message}

import scala.collection.JavaConverters._

class Backlog {
  val spaceId = sys.env("BACKLOG_SPACE_ID")
  val apiKey = sys.env("BACKLOG_API_KEY")
  val projectKey = sys.env("BACKLOG_PROJECT_KEY")
  val configure: BacklogConfigure = new BacklogJpConfigure(spaceId).apiKey(apiKey)
  val client: BacklogClient = new BacklogClientFactory(configure).newClient()

  def fetchComment: Option[Message] = {
    val project = client.getProject(projectKey)
    val activities = client.getProjectActivities(project.getId)
    val activity = activities.asScala.head

    if (activity.getType == Activity.Type.IssueUpdated) {
      val content = activity.getContent.asInstanceOf[IssueUpdatedContent]
      val comment = client.getIssueComment(content.getId, content.getComment.getId)
      Some(Message(
        BuildMessage.updateTitle(content.getSummary, projectKey, content.getKeyId, comment.getCreatedUser.getName, comment.getCreated),
        BuildMessage.updateLink(spaceId, projectKey, content.getKeyId, comment.getId),
        BuildMessage.updateText(content.getComment.getContent)
      ))
    } else if (activity.getType == Activity.Type.IssueCommented) {
      val content = activity.getContent.asInstanceOf[IssueCommentedContent]
      val comment = client.getIssueComment(content.getId, content.getComment.getId)
      Some(Message(
        BuildMessage.commentTitle(content.getSummary, projectKey, content.getKeyId, comment.getCreatedUser.getName, comment.getCreated),
        BuildMessage.commentLink(spaceId, projectKey, content.getKeyId, comment.getId),
        BuildMessage.commentText(content.getComment.getContent)
      ))
    } else {
      None
    }
  }
}
