// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.vcs.git.shared

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.util.application
import com.intellij.vcs.git.shared.branch.GitInOutStateHolder
import com.intellij.vcs.git.shared.repo.GitRepositoriesHolder
import com.intellij.vcs.git.shared.widget.GitWidgetStateHolder

internal class GitDataHoldersInitializer : ProjectActivity {
  override suspend fun execute(project: Project) {
    if (application.isUnitTestMode) return

    GitRepositoriesHolder.getInstance(project).init()
    GitInOutStateHolder.getInstance(project)
    GitWidgetStateHolder.getInstance(project).init()
  }
}
