sys.props.get("plugin.version") match {
  case Some(x) => addSbtPlugin("com.leobenkel" % "soteria" % x)
  case _       => sys.error(
      "The system property 'plugin.version' is not defined. " +
        "Specify this property using the scriptedLaunchOpts -D"
    )
}

// https://github.com/sbt/sbt/issues/6997#issuecomment-1310637232
libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
