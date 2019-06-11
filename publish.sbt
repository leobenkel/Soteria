publishMavenStyle := true

// fail to publish without that
updateOptions := updateOptions.value.withGigahorse(false)

publishArtifact in Test := false

pomIncludeRepository := { _ =>
  false
}
