publishMavenStyle := true

// fail to publish without that
updateOptions := updateOptions.value.withGigahorse(false)

Test / publishArtifact := false

pomIncludeRepository := (_ => false)
