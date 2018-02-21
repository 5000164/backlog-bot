package jp._5000164.backlog_bot.interfaces

import akka.actor.ActorSystem
import jp._5000164.backlog_bot.domain.Message
import slack.api.BlockingSlackApiClient
import slack.models.Attachment

import scala.concurrent.ExecutionContextExecutor

class Slack {
  val token = sys.env("SLACK_TOKEN")
  val postChannel = sys.env("SLACK_POST_CHANNEL")
  implicit val system: ActorSystem = ActorSystem("slack")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  val client = BlockingSlackApiClient(token)

  def post(message: Message): Unit = {
    client.postChatMessage(s"#$postChannel", "", attachments = Some(Seq(Attachment(
      title = Some(message.title),
      title_link = Some(message.link),
      text = Some(message.content),
      pretext = Some(message.pretext)
    ))))
  }
}
