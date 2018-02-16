package jp._5000164.backlog_bot.interfaces

import akka.actor.ActorSystem
import slack.SlackUtil
import slack.rtm.SlackRtmClient

import scala.concurrent.ExecutionContextExecutor

object Application extends App {
  implicit val system: ActorSystem = ActorSystem("slack")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  val token = sys.env("SLACK_TOKEN")
  val client = SlackRtmClient(token)

  val botId = client.state.self.id
  client.onMessage(message => {
    val mentionedIds = SlackUtil.extractMentionedIds(message.text)
    if (mentionedIds.contains(botId)) {
      val speaker = message.user
      if (speaker != botId) {
        client.sendMessage(message.channel, message.text)
      }
    }
  })
}
