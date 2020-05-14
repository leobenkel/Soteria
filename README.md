![Soteria_animated](https://raw.githubusercontent.com/leobenkel/Soteria/master/assets/Soteria_animated.gif)


[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Gitter](https://badges.gitter.im/soteria/community.svg)](https://gitter.im/soteria/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![release-badge][]][release]
[![maven-central-badge][]][maven-central-link]
[![Build Status](https://travis-ci.com/leobenkel/soteria.svg?branch=master)](https://travis-ci.com/leobenkel/soteria)
[![BCH compliance](https://bettercodehub.com/edge/badge/leobenkel/Soteria?branch=master)](https://bettercodehub.com/)
[![Coverage Status](https://coveralls.io/repos/github/leobenkel/Soteria/badge.svg?branch=master)](https://coveralls.io/github/leobenkel/Soteria?branch=master)
[![Mutation testing badge](https://badge.stryker-mutator.io/github.com/leobenkel/Soteria/master)](https://stryker-mutator.github.io)


[release]:              https://github.com/leobenkel/soteria/releases
[release-badge]:        https://img.shields.io/github/tag/leobenkel/soteria.svg?label=version&color=blue
[maven-search]:         https://search.maven.org/search?q=g:com.leobenkel%20a:soteria
[leobenkel-github-badge]:     https://img.shields.io/badge/-Github-yellowgreen.svg?style=social&logo=GitHub&logoColor=black
[leobenkel-github-link]:      https://github.com/leobenkel
[leobenkel-linkedin-badge]:     https://img.shields.io/badge/-Linkedin-yellowgreen.svg?style=social&logo=LinkedIn&logoColor=black
[leobenkel-linkedin-link]:      https://linkedin.com/in/leobenkel
[leobenkel-personal-badge]:     https://img.shields.io/badge/-Website-yellowgreen.svg?style=social&logo=data:image/svg+xml;base64,PHN2ZyBoZWlnaHQ9JzMwMHB4JyB3aWR0aD0nMzAwcHgnICBmaWxsPSIjMDAwMDAwIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIiB2ZXJzaW9uPSIxLjEiIHg9IjBweCIgeT0iMHB4IiB2aWV3Qm94PSIwIDAgNjQgNjQiIGVuYWJsZS1iYWNrZ3JvdW5kPSJuZXcgMCAwIDY0IDY0IiB4bWw6c3BhY2U9InByZXNlcnZlIj48Zz48Zz48cGF0aCBkPSJNNDEuNiwyNy4yYy04LjMsMC0xNSw2LjctMTUsMTVzNi43LDE1LDE1LDE1YzguMywwLDE1LTYuNywxNS0xNVM0OS45LDI3LjIsNDEuNiwyNy4yeiBNNTEuNSwzNmgtMy4zICAgIGMtMC42LTEuNy0xLjQtMy4zLTIuNC00LjZDNDguMiwzMi4yLDUwLjIsMzMuOSw1MS41LDM2eiBNNDEuNiwzMS41YzEuMywxLjIsMi4zLDIuNywzLDQuNGgtNkMzOS4zLDM0LjIsNDAuNCwzMi43LDQxLjYsMzEuNXogICAgIE0zNy40LDMxLjNjLTEsMS40LTEuOCwyLjktMi40LDQuNmgtMy4zQzMzLjEsMzMuOSwzNS4xLDMyLjIsMzcuNCwzMS4zeiBNMzAuMyw0NWMtMC4yLTAuOS0wLjQtMS44LTAuNC0yLjhjMC0xLDAuMS0yLDAuNC0yLjkgICAgaDMuOWMtMC4xLDEtMC4yLDEuOS0wLjIsMi45YzAsMC45LDAuMSwxLjksMC4yLDIuOEgzMC4zeiBNMzEuNyw0OC4zSDM1YzAuNiwxLjcsMS40LDMuNCwyLjQsNC44QzM1LDUyLjIsMzMsNTAuNSwzMS43LDQ4LjN6ICAgICBNNDEuNiw1Mi45Yy0xLjMtMS4yLTIuMy0yLjgtMy4xLTQuNWg2LjFDNDQsNTAuMSw0Mi45LDUxLjcsNDEuNiw1Mi45eiBNMzcuNiw0NWMtMC4yLTAuOS0wLjItMS44LTAuMi0yLjhjMC0xLDAuMS0yLDAuMy0yLjloOCAgICBjMC4yLDAuOSwwLjMsMS45LDAuMywyLjljMCwxLTAuMSwxLjktMC4yLDIuOEgzNy42eiBNNDUuOCw1My4xYzEtMS40LDEuOC0zLDIuNC00LjhoMy4zQzUwLjIsNTAuNSw0OC4yLDUyLjIsNDUuOCw1My4xeiBNNDksNDUgICAgYzAuMS0wLjksMC4yLTEuOCwwLjItMi44YzAtMS0wLjEtMi0wLjItMi45aDMuOWMwLjIsMC45LDAuNCwxLjksMC40LDIuOWMwLDEtMC4xLDEuOS0wLjQsMi44SDQ5eiI+PC9wYXRoPjxwYXRoIGQ9Ik0zNCwyNS45Yy0wLjktMC43LTEuOC0xLjMtMi45LTEuOGMyLTIuMSwzLjItNC45LDMuMi03LjljMC02LjMtNS4xLTExLjQtMTEuNC0xMS40UzExLjYsOS45LDExLjYsMTYuMiAgICBjMCwzLjEsMS4yLDUuOSwzLjIsNy45Yy00LjEsMi02LjgsNS40LTcuMSw5LjRsLTAuMywzLjhjMCwyLDcsMy42LDE1LjYsMy42YzAuMiwwLDAuNSwwLDAuNywwQzI0LjIsMzQuMywyOC4yLDI4LjYsMzQsMjUuOXogICAgIE0yMyw4LjhjNC4xLDAsNy40LDMuMyw3LjQsNy40cy0zLjMsNy40LTcuNCw3LjRzLTcuNC0zLjMtNy40LTcuNFMxOC45LDguOCwyMyw4Ljh6Ij48L3BhdGg+PC9nPjwvZz48L3N2Zz4=&logoColor=black
[leobenkel-personal-link]:      https://leobenkel.com
[leobenkel-patreon-link]:            https://www.patreon.com/leobenkel
[leobenkel-patreon-badge]: https://img.shields.io/badge/-Patreon-yellowgreen.svg?style=social&logo=Patreon&logoColor=black
[maven-central-link]:                             https://maven-badges.herokuapp.com/maven-central/com.leobenkel/soteria
[maven-central-badge]:          https://maven-badges.herokuapp.com/maven-central/com.leobenkel/soteria/badge.svg


# Soteria

Information about [who was Soteria](https://www.wikiwand.com/en/Soteria_(mythology)).

If you have any question [submit an issue](https://github.com/leobenkel/soteria/issues/new).

## Table of Contents

  * [Setup steps](#setup-steps)
  * [soteria.json](#soteriajson)
     * [Root level](#root-level)
     * [Modules](#modules)
        * [How to make sure library A is always version x.y and Provided ?](#how-to-make-sure-library-a-is-always-version-xy-and-provided-)
        * [How to remove a dependency D completely from a library A ?](#how-to-remove-a-dependency-d-completely-from-a-library-a-)
        * [Is there an easy way to build the dependenciesToRemove tree ?](#is-there-an-easy-way-to-build-the-dependenciestoremove-tree-)
  * [Features](#features)
     * [Scala Style](#scala-style)
     * [Coveralls](#coveralls)
     * [For fat-jar assembly build](#for-fat-jar-assembly-build)
     * [!!!Dangerous!!! Allow compilation even with vulnerability](#dangerous-allow-compilation-even-with-vulnerability)
        * [Vulnerability by-pass](#vulnerability-by-pass)
        * [Compilation warning by pass](#compilation-warning-by-pass)
     * [Debug](#debug)
  * [Publishing](#publishing)
  * [Authors](#authors)
     * [Leo Benkel](#leo-benkel)

Created by [gh-md-toc](https://github.com/ekalinin/github-markdown-toc)


## Setup steps

1. Make sure you are using `SBT 1.2.x`.
1. Add to `./project/soteria.sbt` in your project:
    ```
    addSbtPlugin("com.leobenkel" % "soteria" % soteriaVersion)
    ```
    The latest release is [![release-badge][]][release] [![maven-central-badge][]][maven-central-link]
1. Make sure to have a config file. Take a look at [soteria.json](https://github.com/leobenkel/soteria/blob/master/soteria.json) for examples
1. If you need a **fat-jar**:
     1. Add to your `build.sbt` the following lines:
         ```
         assemblyOption in assembly := soteriaAssemblySettings.value
         enablePlugins(DockerPlugin)
         ```
1. Clean up your `build.sbt` by removing all pre-set settings:
     1. `dependencyOverrides`
     1. `scalacOptions`
     1. All settings related to `sbt-assembly` and `sbt-docker`
     1. All test options `in Test`:
        1. `javaOptions in Test`
        1. `testOptions in Test`
        1. `parallelExecution in Test`
        1. `fork in Test`
1. Remove all plugins that are already included for you:
     1. `sbt-scoverage`
     1. `sbt-assembly`
     1. `sbt-docker`
     1. `sbt-dependency-graph`
     1. `sbt-scalafix`
     1. `sbt-scalafmt`
     1. `scalastyle-sbt-plugin`
1. You can run your project the same as before. There can be compilation issues due to vulnerable dependencies.
1. *Dangerous!* If you are not able to fix compiler issues, add: `soteriaSoftOnCompilerWarning := true`
1. *Dangerous!* If you are not able to fix dependencies issues, add: `soteriaSoft := true`
1. You can now then fix scala style issues:
    1. Run `sbt soteriaCheckScalaStyle`
    1. Run `sbt soteriaCheckScalaFix`
    1. Run `sbt soteriaCheckScalaFmt`
        1. If you are ready to rewrite the broken files:
            1. Create a clean branch
            1. Run `sbt soteriaCheckScalaFmtRun`

## soteria.json

### Root level

To override where the config file is read from, update the setting `soteriaConfPath` in your `build.sbt`. This setting can be a URL starting by `http://` or `https://` or a local file path. 
By default it will search for `./soteria.json`.

Root level:

* `dockerImage`: To set in which image the fat-jar will be built.
* `sbtVersion`: SBTVersion to enforce. If a project is trying to compile with a different version, it will break.
* `scalaVersions`: Is an array of authorized scala Version. This is an array and not a value to allow Spark/Play project on different version.
* `scalaCFlags`: The list of compile flag to add to the build process. If `soteriaSoftOnCompilerWarning` is **not** true, then `-Xfatal-warnings` will be added as well to trigger a compilation failure.
* `modules`: This is where the bulk of the settings are living.
  * The structure is `groupId|com.organization` -> `artifactName` -> description of the constraints
  
### Modules

The modules are the constraint enforced by the plugin related to each dependencies.

The path to each module is `groupId|com.organization` -> `artifactName` -> description of the constraints.

A module can accept those keys:
* `version`: Either `None` or a version number. If the library is added with a different version number, the compilation will fail
* `exactName`: Default is True if absent. If false, the `artifactName` can just be a start. It is used for instance where you want to enforce a version for a library and related ones. `circe` or `spark-` can be good examples.
* `excludeName`: Default empty. It is used to exclude libraries that would be catch by the `name` + `exactName`:false. It is used for instance in Play project where you would enforce something for all libraries starting by `play-` except a few that are behind on version numbers.
* `needDoublePercent`: By default is false. If true, the conversion to `sbt.ModuleID` will be with `%%` instead of `%`. The same way it would be in the `build.sbt`.
* `shouldDownload`: Is true by default and is only used for `sbt soteriaGetAllDependencies`.
* `overrideIsEnough`: Default is true. This is related to `dependenciesToRemove`. 
  * If `overrideIsEnough` is true, the library will be added to `dependencyOverrides`. 
  * If `overrideIsEnough` is false, the library will be converted to an exclusion rule.
* `forbidden`: Default is null. If this is set, and the library is added, the message will be displayed as a build failure. For instance you can use it to forbid one MySQL library and advise to use a different one.
* `shouldBeProvided`: Default is false. If true, the compilation will fail if the library is not set to `Provided` in `build.sbt`. It is used for Spark.
* `dependenciesToRemove`: This is a list of `groupID | artifactName` libraries to remove from this library. This is when `overrideIsEnough` come into play.
* `scalaVersionsFilter`: This field is a list of scala versions, following this format: `[+-][MajorVersion].[MinorVersion]<.[SmallVersion]>`.

#### How to make sure library A is always version x.y and Provided ?

```
"modules": {
    "groupID.A": {
        "artifactName-A": {
            "version": "x.y",
            "shouldBeProvided": true
        }
    }
}
```

#### How to remove a dependency D completely from a library A ?

```
"modules": {
    "groupID.D": {
        "artifactName-D": {
            "version": "None",
            "overrideIsEnough": false
        }
    },
    "groupID.A": {
        "artifactName-A": {
            "dependenciesToRemove": [ 
                "groupID.D | artifactName-D"
            ],
            "version": "vA.A"
        }
    }
}
```


The `dependenciesToRemove` in A, will search for D. 
Since `overrideIsEnough` is false in D, the plugin will remove D from A using an ExclusionRule.
Then, the plugin gather all the library which have been removed, and add them back with the appropriate version.
In this case the version of D is `None`, so it will **not** be added back. 

#### Is there an easy way to build the `dependenciesToRemove` tree ?

Yes there is !

First assemble your json:
 
 ```
"modules": {
    "groupID.D": {
        "artifactName-D": {
            "version": "None",
            "overrideIsEnough": false
        }
    },
    "groupID.A": {
        "artifactName-A": {
            "version": "vA.A"
        }
    },
    "groupID.B": {
        "artifactName-B": {
            "version": "vB.B"
        }
    }
}
```

Then run `sbt soteriaDebugAllModules`.

This will:
 
1. Remove all the dependencies from your `build.sbt`
1. List all the known libraries from your config file
1. Add one library at a time, compile and get the fetched dependencies
1. Compare the fetch dependencies with the known dependencies from your config file
1. When all the libraries have been reviewed, the plugin will display a new json payload that you can just copy paste with all the `dependenciesToRemove` set to the knowledge you have in your json.

## Features

### Scala Style

The sbt plugin includes [ScalaFix](https://github.com/scalacenter/scalafix), [ScalaStyle](http://www.scalastyle.org/) and [ScalaFmt](https://scalameta.org/scalafmt/).


1. Check that you have [.scalafix.conf](https://github.com/leobenkel/soteria/blob/master/.scalafix.conf), [.scalafmt.conf](https://github.com/leobenkel/soteria/blob/master/.scalafmt.conf) and [scalastyle-config.xml](https://github.com/leobenkel/soteria/blob/master/scalastyle-config.xml) in your project
   * Feel free to copy the one present in this repo to follow the same style guides
2. Run `sbt soteriaCheckScalaCheckAll` to check that everything is correct.
    * You can run each system independently with:
        * `sbt soteriaCheckScalaStyle`
        * `sbt soteriaCheckScalaFix`
        * `sbt soteriaCheckScalaFmt`
3. To apply the fix for [ScalaFmt](https://scalameta.org/scalafmt/), you can run `sbt soteriaCheckScalaFmtRun`

### Coveralls

#### Locally

You can run 
```
sbt soteriaRunTestCoverage
```
to generate local reporting.

Just open:
```
./target/scala-2.xx/sbt-1.0/scoverage-report/index.html
```

#### Submit to coveralls

To submit to coveralls
```
sbt soteriaRunSubmitCoverage
```

This will generate the reports and submit it to coveralls using your COVERALLS token, set in the env var: `COVERALLS_REPO_TOKEN`. 

### For fat-jar assembly build

You need to add
```
assemblyOption in assembly := soteriaAssemblySettings.value
enablePlugins(DockerPlugin)
```

to your `build.sbt` file.

You can now call `sbt docker` to create the fat-jar. It will be located at `./target/docker/0/*.jar`.

To change in which docker image the build is ran, you can change `dockerImage` in [soteria.json](https://github.com/leobenkel/soteria/blob/master/soteria.json).

### !!!Dangerous!!! Allow compilation even with vulnerability

#### Vulnerability by-pass

By default, you won't be able to compile if you have errors in your build.

If you need time to fix several issues, you can add:

```
soteriaSoft := true
```

to your `build.sbt` file while you are fixing them.

#### Compilation warning by pass

If you also want to allow compiler warning, you will need to add:

```
soteriaSoftOnCompilerWarning := true
```

to your `build.sbt` file while you are fixing them

### Debug

To print more or less logs for this plugin, you can set `soteriaLogLevel`.

For `Debug`:

```
soteriaLogLevel := Level.Debug
```

For `Error` only:

```
soteriaLogLevel := Level.Error
```

## Publishing

* Update version number in `VERSION` file.
* Deploy the updated plugin locally: `make publishLocal`
* Update version number in `./project/soteria.sbt`.
* Run the plugin on itself with `make publishLocal`
* Publish: `make publish`

## Authors

### Leo Benkel

* [![leobenkel-github-badge][]][leobenkel-github-link]
* [![leobenkel-linkedin-badge][]][leobenkel-linkedin-link]
* [![leobenkel-personal-badge][]][leobenkel-personal-link]
* [![leobenkel-patreon-badge][]][leobenkel-patreon-link]

