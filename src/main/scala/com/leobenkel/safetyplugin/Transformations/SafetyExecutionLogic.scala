package com.leobenkel.safetyplugin.Transformations

private[safetyplugin] object SafetyExecutionLogic
    extends TaskAllDependencies with TaskLog with TaskAssembly with TaskConfiguration
    with TaskDebugModule with TaskDependencyOverrides with TaskLibraryDependencies with TaskScalac
    with TaskUpdate with TaskVersions with TaskGetAllDependencies with TaskCheckDependencies {
}
