package jp._5000164.backlog_bot.infrastructure

import jp._5000164.backlog_bot.infrastructure.Settings.{ProjectKey, RepositoryName}

import scala.io.Source
import scala.reflect.runtime.{currentMirror, universe}
import scala.tools.reflect.ToolBox


object Settings {
  val toolbox: ToolBox[universe.type] = currentMirror.mkToolBox()
  val settings: SettingsType = toolbox.eval(toolbox.parse(Source.fromResource("Settings.scala").mkString)).asInstanceOf[SettingsType]

  type ProjectKey = String
  type RepositoryName = String
}

trait SettingsType {
  type Projects = Map[ProjectKey, Project]
  val projects: Projects
}

case class Project(
    issue: Issue,
    wiki: Wiki,
    repositories: Map[RepositoryName, Repository]
)

case class Issue(
    postChannel: String
)

case class Wiki(
    postChannel: String
)

case class Repository(
    postChannel: String
)
