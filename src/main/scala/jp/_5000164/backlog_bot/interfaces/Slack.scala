package jp._5000164.backlog_bot.interfaces

import akka.actor.ActorSystem
import jp._5000164.backlog_bot.domain.MessageBundle
import slack.api.BlockingSlackApiClient
import slack.models.Attachment

import scala.concurrent.ExecutionContextExecutor

class Slack {
  val token = sys.env("SLACK_TOKEN")
  implicit val system: ActorSystem = ActorSystem("slack")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  val client = BlockingSlackApiClient(token)

  def post(messageBundles: List[MessageBundle]): Unit =
    messageBundles.foreach(messageBundle =>
      messageBundle.messages.foreach(message =>
        client.postChatMessage(
          s"#${messageBundle.postChannel}",
          "",
          attachments = Some(Seq(Attachment(
            author_name = message.authorName,
            pretext = message.pretext,
            title = message.title,
            title_link = message.link,
            text = message.text
          ))))
      )
    )
}
