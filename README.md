[![Build Status](https://travis-ci.org/leobenkel/safety_plugin.svg?branch=master)](https://travis-ci.org/leobenkel/safety_plugin)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

# sbt_safety_plugin

## Setup steps

If you have any question [ask Leo](https://github.com/leobenkel).

1. Make sure you are using `SBT 1.2.6`.
1. Add to `./project/safety.sbt` in your project:
    ```
    addSbtPlugin("com.leobenkel" % "safety_plugin" % "<VERSION>")
    ```
1. Make sure to have a config file. Take a look at `safetyPlugin.json` for examples
1. If you need a fat-jar:
     1. Add to your `build.sbt` the following lines:
         ```
         assemblyOption in assembly := safetyAssemblySettings.value
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
1. If you are not able to fix compiler issues, add: `safetySoftOnCompilerWarning := true`
1. If you are not able to fix dependencies issues, add: `safetySoft := true`
1. You can now fix scala style issues (You should fix each step in separate branches for easy review):
    1. Run `sbt safetyCheckScalaStyle`
        1. Fix all the issues
    1. Run `sbt safetyCheckScalaFix`
        1. Fix all the issues
    1. Run `sbt safetyCheckScalaFmt`
        1. If you are ready to rewrite the broken files:
            1. Create a clean branch
            1. Run `sbt safetyCheckScalaFmtRun`

## Features

### Scala Style

The sbt plugin now includes ScalaFix, ScalaStyle and ScalaFmt.

To check that you are following the right styling do:
1. Check that you have `.scalafix.conf`, `.scalafmt.conf` and `scalastyle-config.xml` in your project
   * Feel free to copy the one present in this repo to follow the same style guides
2. Run `sbt safetyCheckScalaCheckAll` to check that everything is correct.
    * You can run each system independently with:
        * `safetyCheckScalaStyle`
        * `safetyCheckScalaFix`
        * `safetyCheckScalaFmt`
3. To apply the fix for `ScalaFMT`, you can run `sbt safetyCheckScalaFmtRun`.
    * Be sure to be in a clean branch as the changes might be huge.

### For fat-jar assembly build

You need to add
```
assemblyOption in assembly := safetyAssemblySettings.value
enablePlugins(DockerPlugin)
```

to your `build.sbt` file.

You can now call `sbt docker` to create the fat-jar. It will be located at `./target/docker/0/*.jar`.

### !!!Dangerous!!! Allow compilation even with vulnerability

#### Vulnerability by pass

By default, you won't be able to compile if you have errors in your build.

If you need time to fix several issues, you can add:
```
safetySoft := true
```

to your `build.sbt` file while you are fixing them.

#### Compilation warning by pass

If you also want to allow compiler warning, you will need to add:

```
safetySoftOnCompilerWarning := true
```

to your `build.sbt` file while you are fixing them

### Debug

To print more or less logs for this plugin, you can set `safetyLogLevel`.

For `Debug`:

```
safetyLogLevel := Level.Debug
```

For `Error` only:

```
safetyLogLevel := Level.Error
```

## Publishing

* Update version number in `VERSION` file.
* Deploy the updated plugin locally: `make publishLocal`
* Update version number in `./project/safety.sbt`.
* Run the plugin on itself with `make publishLocal`
* Publish: `make publish`

