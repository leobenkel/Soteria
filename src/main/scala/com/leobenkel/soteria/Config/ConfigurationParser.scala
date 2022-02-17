package com.leobenkel.soteria.Config

import com.leobenkel.soteria.Utils.Json.JsonDecode
import com.leobenkel.soteria.Utils.LoggerExtended
import scala.io.Source
import scala.util._

private[soteria] case class ConfigurationParser(
    log:        LoggerExtended,
    configPath: String,
) {
  if(!configPath.endsWith(".json")) log.criticalFailure(
    s"The input configuration file was defined as '$configPath' " +
      s"but it should be a '.json' file."
  )

  @transient lazy private val isWeb: Boolean =
    configPath.startsWith("http://") || configPath.startsWith("https://")

  @transient lazy private val fileContent: String =
    Try(
      if(isWeb) Source.fromURL(configPath)
      else Source.fromFile(configPath)
    ) match {
      case Success(file)      =>
        val content = file.mkString
        file.close()
        content
      case Failure(exception) =>
        log.criticalFailure(exception.toString)
        throw exception
    }

  @transient lazy private val conf: SoteriaConfiguration =
    JsonDecode.parse[SoteriaConfiguration](fileContent)(
      SoteriaConfiguration.parser(log)
    ) match {
      case Left(err: String) =>
        log.criticalFailure(err)
        throw new Exception(err)
      case Right(c)          => c
    }

  def getConf: SoteriaConfiguration = conf
}
