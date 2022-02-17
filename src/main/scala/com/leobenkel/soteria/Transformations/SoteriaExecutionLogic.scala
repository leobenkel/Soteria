package com.leobenkel.soteria.Transformations

private[soteria] trait SoteriaExecutionLogic
    extends TaskAllDependencies
    with TaskLog
    with TaskAssembly
    with TaskConfiguration
    with TaskDebugModule
    with TaskDependencyOverrides
    with TaskScalac
    with TaskUpdate
    with TaskGetAllDependencies
    with TaskCheckDependencies
    with AddScalaFixCompilerPlugin
    with CheckEnvIsSetUp {}
