package jp._5000164.backlog_bot.interfaces

import java.nio.file.{Files, Paths}

import scala.io.Source

class Reader {
  def readRecord(): Option[String] =
    if (Files.notExists(Paths.get(".record"))) None
    else Some(Source.fromFile(".record").mkString)
}
