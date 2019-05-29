publishMavenStyle := true

// fail to publish without that
updateOptions := updateOptions.value.withGigahorse(false)

publishTo := Some("MAVEN" at
  "FILL ME")

publishArtifact in Test := false

pomIncludeRepository := { _ => false }
