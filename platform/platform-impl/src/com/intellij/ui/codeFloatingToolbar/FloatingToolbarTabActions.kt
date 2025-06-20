// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.ui.codeFloatingToolbar

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.Key
import com.intellij.util.ui.UIUtil
import org.jetbrains.annotations.Unmodifiable

internal sealed class FloatingToolbarTabAction : AnAction() {
  init {
    isEnabledInModalContext = true
    templatePresentation.putClientProperty(FLOATING_TOOLBAR_ACTION_MARKER, true)
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabledAndVisible = (findFloatingToolbar(e) != null)
  }
}

internal class GotoNextFloatingToolbarMenu: FloatingToolbarTabAction() {
  override fun actionPerformed(e: AnActionEvent) {
    showNextMenu(e, isForwardDirection = true)
  }
}

internal class GotoPreviousFloatingToolbarMenu: FloatingToolbarTabAction() {
  override fun actionPerformed(e: AnActionEvent) {
    showNextMenu(e, isForwardDirection = false)
  }
}

internal class FloatingToolbarTabActionPromoter : ActionPromoter {
  override fun promote(actions: @Unmodifiable List<AnAction>, context: DataContext): @Unmodifiable List<AnAction> {
    return actions.sortedWith { action1, action2 ->
      val isTabAction1 = action1.templatePresentation.getClientProperty(FLOATING_TOOLBAR_ACTION_MARKER) == true
      val isTabAction2 = action2.templatePresentation.getClientProperty(FLOATING_TOOLBAR_ACTION_MARKER) == true
      when {
        isTabAction1 && !isTabAction2 -> -1
        !isTabAction1 && isTabAction2 -> 1
        else -> 0
      } 
    }
  }
}

private val FLOATING_TOOLBAR_ACTION_MARKER = Key.create<Boolean>("FLOATING_TOOLBAR_ACTION_MARKER")

private fun showNextMenu(event: AnActionEvent, isForwardDirection: Boolean) {
  val project = event.project ?: return
  val floatingToolbar = findFloatingToolbar(event) ?: return
  val hintComponent = floatingToolbar.hintComponent ?: return
  val allButtons = UIUtil.findComponentsOfType(hintComponent, ActionButton::class.java)
  val navigatableButtons = allButtons.filter { it.presentation.isEnabledAndVisible }
  val selectedIndex = navigatableButtons.indexOfFirst { button -> button.isSelected }
  if (selectedIndex < 0) return
  val nextIndex = if (isForwardDirection) selectedIndex + 1 else selectedIndex - 1
  val button = navigatableButtons[nextIndex.mod(navigatableButtons.size)]
  if (button.presentation.isPopupGroup) {
    button.click()
  }
  else {
    showActionInPopup(project, floatingToolbar, button)
  }
}

private fun findFloatingToolbar(e: AnActionEvent): CodeFloatingToolbar? {
  val project = e.project ?: return null
  val selectedEditor = FileEditorManager.getInstance(project).getSelectedTextEditor()
  val toolbar = CodeFloatingToolbar.getToolbar(selectedEditor) ?: return null
  if (!toolbar.isShown()) return null
  return toolbar
}

private fun showActionInPopup(project: Project, floatingToolbar: CodeFloatingToolbar, button: ActionButton){
  val editor = FileEditorManager.getInstance(project).getSelectedTextEditor() ?: return
  val popup = JBPopupFactory.getInstance().createActionGroupPopup(
    null,
    DefaultActionGroup(button.action),
    DataManager.getInstance().getDataContext(editor.component),
    JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
    true,
    ActionPlaces.EDITOR_FLOATING_TOOLBAR,
  )
  floatingToolbar.attachPopupToButton(button, popup)
  popup.showUnderneathOf(button)
}
