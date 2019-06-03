package com.leobenkel.safetyplugin.Transformations

import sbtassembly.{MergeStrategy, PathList}

/**
  * Stryker4s was mutating this method and making it too large for the JVM.
  * Moved to its own place so it is easier to deal with
  */
private[Transformations] trait MergeStrategyConfiguration {

  // scalastyle:off cyclomatic.complexity
  def getMergeStrategy(
    input:       String,
    oldStrategy: String => MergeStrategy
  ): MergeStrategy = {
    input match {
      case "git.properties"                           => MergeStrategy.rename
      case "mime.types"                               => MergeStrategy.filterDistinctLines
      case "overview.html"                            => MergeStrategy.rename
      case "BUILD"                                    => MergeStrategy.rename
      case "module-info.class"                        => MergeStrategy.rename
      case "play/reference-overrides.conf"            => MergeStrategy.rename
      case PathList("META-INF", _ @_*)                => MergeStrategy.rename
      case PathList("com", "databricks", _ @_*)       => MergeStrategy.last
      case PathList("org", "slf4j", _ @_*)            => MergeStrategy.last
      case PathList("org", "apache", _ @_*)           => MergeStrategy.last
      case PathList("javax", "inject", _ @_*)         => MergeStrategy.last
      case PathList("javax", "servlet", _ @_*)        => MergeStrategy.last
      case PathList("javax", "ws", _ @_*)             => MergeStrategy.last
      case PathList("javax", "xml", _ @_*)            => MergeStrategy.last
      case PathList("javax", "annotation", _ @_*)     => MergeStrategy.last
      case PathList("com", "sun", _ @_*)              => MergeStrategy.last
      case PathList("com", "codahale", _ @_*)         => MergeStrategy.last
      case PathList("org", "glassfish", _ @_*)        => MergeStrategy.last
      case PathList("org", "aopalliance", _ @_*)      => MergeStrategy.last
      case PathList("org", "objectweb", "asm", _ @_*) => MergeStrategy.last
      case PathList("jersey", "repackaged", _ @_*)    => MergeStrategy.last
      case PathList("io", "netty", _ @_*)             => MergeStrategy.last
      case PathList("mozilla", _ @_*)                 => MergeStrategy.last
      case x                                          => oldStrategy(x)
    }
  }

  // scalastyle:on
}
