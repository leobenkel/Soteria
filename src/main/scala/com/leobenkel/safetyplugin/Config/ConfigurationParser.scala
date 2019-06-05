package com.leobenkel.safetyplugin.Config

import com.leobenkel.safetyplugin.Utils.Json.JsonDecode
import com.leobenkel.safetyplugin.Utils.LoggerExtended

import scala.io.Source
import scala.util._

private[safetyplugin] case class ConfigurationParser(
  log:        LoggerExtended,
  configPath: String
) {
  if (!configPath.endsWith(".json")) {
    log.criticalFailure(
      s"The input configuration file was defined as '$configPath' " +
        s"but it should be a '.json' file."
    )
  }

  @transient lazy private val fileContent: String = Try(Source.fromFile(configPath)) match {
    case Success(file) =>
      val content = file.mkString
      file.close()
      content
    case Failure(exception) =>
      log.criticalFailure(exception.toString)
      throw exception
  }

  @transient lazy private val conf: SafetyConfiguration = {
    JsonDecode.parse[SafetyConfiguration](fileContent) match {
      case Left(err: String) =>
        log.criticalFailure(err)
        throw new Exception(err)
      case Right(c) => c
    }
  }

  def getConf: SafetyConfiguration = conf
}
