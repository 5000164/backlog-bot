package jp._5000164.backlog_bot.interfaces

import com.nulabinc.backlog4j.conf.{BacklogConfigure, BacklogJpConfigure}
import com.nulabinc.backlog4j.internal.json.activities.IssueUpdatedContent
import com.nulabinc.backlog4j.{BacklogClient, BacklogClientFactory}
import jp._5000164.backlog_bot.domain.Message

import scala.collection.JavaConverters._

class Backlog {
  val spaceId = sys.env("BACKLOG_SPACE_ID")
  val apiKey = sys.env("BACKLOG_API_KEY")
  val projectKey = sys.env("BACKLOG_PROJECT_KEY")
  val configure: BacklogConfigure = new BacklogJpConfigure(spaceId).apiKey(apiKey)
  val client: BacklogClient = new BacklogClientFactory(configure).newClient()

  def fetchComment: Message = {
    val project = client.getProject(projectKey)
    val activities = client.getProjectActivities(project.getId)
    val content = activities.asScala.head.getContent.asInstanceOf[IssueUpdatedContent]

    Message(content.getSummary, content.getComment.getContent, s"https://$spaceId.backlog.jp/view/$projectKey-${content.getKeyId}#comment-${content.getComment.getIdAsString}")
  }
}
