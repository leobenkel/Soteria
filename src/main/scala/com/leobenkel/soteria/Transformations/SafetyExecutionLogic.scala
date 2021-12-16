package com.leobenkel.soteria.Transformations

private[soteria] object SoteriaExecutionLogic
    extends TaskAllDependencies
    with TaskLog
    with TaskAssembly
    with TaskConfiguration
    with TaskDebugModule
    with TaskDependencyOverrides
    with TaskLibraryDependencies
    with TaskScalac
    with TaskUpdate
    with TaskVersions
    with TaskGetAllDependencies
    with TaskCheckDependencies
    with AddScalaFixCompilerPlugin
    with CheckEnvIsSetUp {}
