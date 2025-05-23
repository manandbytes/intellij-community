// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.ide.actions;

import com.intellij.psi.PsiDirectory;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import javax.swing.Icon;
import java.util.Collection;

/**
 * Implement this contributor to add useful and often used directories in the 'Create Directory' popup.<br>
 * E.g. Gradle or Maven could add conventional source directories ('src/main/java', 'src/main/test')
 */
public interface CreateDirectoryCompletionContributor {
  /**
   * @return A short description for the suggested variants, to be shown as a group's title in the completion list,
   * e.g. 'Gradle Source Sets', 'Maven Source Directories'.
   */
  @NotNull
  @Nls(capitalization = Nls.Capitalization.Sentence)
  String getDescription();

  /**
   * @return completion subdirectory variant for the selected directory
   */
  @RequiresReadLock
  @NotNull
  Collection<@NotNull Variant> getVariants(@NotNull PsiDirectory directory);

  final class Variant {
    final @NotNull String path;
    final @Nullable JpsModuleSourceRootType<?> rootType;
    final @Nullable Icon icon;

    public Variant(@NotNull String path, @Nullable JpsModuleSourceRootType<?> rootType) {
      this( path, rootType, null);
    }

    /**
     * @param path     absolute or relative path to a directory
     * @param rootType root type with which the created directory will be marked automatically marked
     */
    public Variant(@NotNull String path, @Nullable JpsModuleSourceRootType<?> rootType, @Nullable Icon icon) {
      this.path = path;
      this.rootType = rootType;
      this.icon = icon;
    }

    public @NotNull String getPath() {
      return path;
    }

    public @Nullable JpsModuleSourceRootType<?> getRootType() {
      return rootType;
    }

    public @Nullable Icon getIcon() {
      return icon;
    }
  }
}
