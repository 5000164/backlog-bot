package jp._5000164.backlog_bot.interfaces

import akka.actor.ActorSystem
import com.nulabinc.backlog4j.BacklogClientFactory
import com.nulabinc.backlog4j.api.option.GetIssuesParams
import com.nulabinc.backlog4j.conf.BacklogJpConfigure
import slack.SlackUtil
import slack.rtm.SlackRtmClient

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContextExecutor

object Application extends App {
  implicit val system: ActorSystem = ActorSystem("slack")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  val token = sys.env("SLACK_TOKEN")
  val slackClient = SlackRtmClient(token)

  val botId = slackClient.state.self.id
  slackClient.onMessage(message => {
    val mentionedIds = SlackUtil.extractMentionedIds(message.text)
    if (mentionedIds.contains(botId)) {
      val speaker = message.user
      if (speaker != botId) {
        val spaceId = sys.env("BACKLOG_SPACE_ID")
        val apiKey = sys.env("BACKLOG_API_KEY")
        val projectKey = sys.env("BACKLOG_PROJECT_KEY")
        val configure = new BacklogJpConfigure(spaceId).apiKey(apiKey)
        val backlogClient = new BacklogClientFactory(configure).newClient()
        val project = backlogClient.getProject(projectKey)
        val issues = backlogClient.getIssues(new GetIssuesParams(List(project.getId).asJava))
        val lastIssue = issues.asScala.head.getIdAsString
        val comments = backlogClient.getIssueComments(lastIssue)
        val comment = comments.asScala.head
        slackClient.sendMessage(message.channel, comment.getCreatedUser.getName + "\n\n" + comment.getContent)
      }
    }
  })
}
