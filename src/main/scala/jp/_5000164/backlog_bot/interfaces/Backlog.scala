package jp._5000164.backlog_bot.interfaces

import java.util.Date

import com.nulabinc.backlog4j._
import com.nulabinc.backlog4j.api.option.QueryParams
import com.nulabinc.backlog4j.conf.{BacklogConfigure, BacklogJpConfigure}
import com.nulabinc.backlog4j.internal.json.activities._
import jp._5000164.backlog_bot.domain.{Message, MessageBundle}
import jp._5000164.backlog_bot.infractructure.Settings

import scala.collection.JavaConverters._

class Backlog {
  val spaceId = sys.env("BACKLOG_SPACE_ID")
  val apiKey = sys.env("BACKLOG_API_KEY")
  val configure: BacklogConfigure = new BacklogJpConfigure(spaceId).apiKey(apiKey)
  val client: BacklogClient = new BacklogClientFactory(configure).newClient()

  def fetchMessages(lastExecutedAt: Date, projects: Settings.settings.Projects): Seq[MessageBundle] =
    projects.keys.toSeq.flatMap(
      projectKey => {
        val project = client.getProject(projectKey)
        val activities = client.getProjectActivities(project.getId)

        activities.asScala.filter(_.getCreated after lastExecutedAt).flatMap {
          case activity if activity.getType == Activity.Type.IssueCreated =>
            val content = activity.getContent.asInstanceOf[IssueCreatedContent]
            try {
              val issue = client.getIssue(content.getId)

              val postChannel = projects(projectKey).issue.postChannel
              val message = Message.build(spaceId, projectKey, activity, content, issue)

              Some(MessageBundle(postChannel, message))
            } catch {
              case _: BacklogException => None
            }

          case activity if activity.getType == Activity.Type.IssueUpdated =>
            val content = activity.getContent.asInstanceOf[IssueUpdatedContent]
            val comment = client.getIssueComment(content.getId, content.getComment.getId)

            val postChannel = projects(projectKey).issue.postChannel
            val message = Message.build(spaceId, projectKey, activity, content, comment)

            Some(MessageBundle(postChannel, message))

          case activity if activity.getType == Activity.Type.IssueCommented =>
            val content = activity.getContent.asInstanceOf[IssueCommentedContent]
            val comment = client.getIssueComment(content.getId, content.getComment.getId)

            val postChannel = projects(projectKey).issue.postChannel
            val message = Message.build(spaceId, projectKey, activity, content, comment)

            Some(MessageBundle(postChannel, message))

          case activity if activity.getType == Activity.Type.WikiCreated =>
            val content = activity.getContent.asInstanceOf[WikiCreatedContent]

            val postChannel = projects(projectKey).wiki.postChannel
            val message = Message.build(spaceId, projectKey, activity, content)

            Some(MessageBundle(postChannel, message))

          case activity if activity.getType == Activity.Type.WikiUpdated =>
            val content = activity.getContent.asInstanceOf[WikiUpdatedContent]

            val postChannel = projects(projectKey).wiki.postChannel
            val message = Message.build(spaceId, projectKey, activity, content)

            Some(MessageBundle(postChannel, message))

          case activity if activity.getType == Activity.Type.GitPushed =>
            val content = activity.getContent.asInstanceOf[GitPushedContent]

            projects(projectKey).repositories.get(content.getRepository.getName) match {
              case Some(repository) =>
                val postChannel = repository.postChannel
                val message = Message.build(spaceId, projectKey, activity, content)

                Some(MessageBundle(postChannel, message))
              case None => None
            }

          case activity if activity.getType == Activity.Type.PullRequestAdded =>
            val content = activity.getContent.asInstanceOf[PullRequestContent]
            val pullRequest = client.getPullRequest(project.getId, content.getRepository.getId, content.getNumber)

            projects(projectKey).repositories.get(content.getRepository.getName) match {
              case Some(repository) =>
                val postChannel = repository.postChannel
                val message = Message.build(spaceId, projectKey, activity, content, pullRequest)

                Some(MessageBundle(postChannel, message))
              case None => None
            }

          case activity if activity.getType == Activity.Type.PullRequestUpdated =>
            val content = activity.getContent.asInstanceOf[PullRequestContent]
            val comment = client.getPullRequestComments(project.getId, content.getRepository.getId, content.getNumber, (new QueryParams).minId(content.getComment.getId - 1).maxId(content.getComment.getId + 1).count(1)).toArray.head.asInstanceOf[PullRequestComment]

            projects(projectKey).repositories.get(content.getRepository.getName) match {
              case Some(repository) =>
                val postChannel = repository.postChannel
                val message = Message.build(spaceId, projectKey, activity, content, comment)

                Some(MessageBundle(postChannel, message))
              case None => None
            }

          case _ =>
            None
        }
      }
    )
}
