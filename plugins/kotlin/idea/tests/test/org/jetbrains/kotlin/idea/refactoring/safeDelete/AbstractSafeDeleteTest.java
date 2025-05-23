// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.jetbrains.kotlin.idea.refactoring.safeDelete;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.platform.testFramework.core.FileComparisonFailedError;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.safeDelete.SafeDeleteHandler;
import com.intellij.testFramework.LightProjectDescriptor;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.idea.base.test.IgnoreTests;
import org.jetbrains.kotlin.idea.test.KotlinLightCodeInsightFixtureTestCase;
import org.jetbrains.kotlin.idea.test.KotlinLightCodeInsightFixtureTestCaseKt;
import org.jetbrains.kotlin.idea.test.KotlinWithJdkAndRuntimeLightProjectDescriptor;
import org.jetbrains.kotlin.psi.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractSafeDeleteTest extends KotlinLightCodeInsightFixtureTestCase {
    @NotNull
    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return KotlinWithJdkAndRuntimeLightProjectDescriptor.getInstance();
    }

    public void doClassTest(@NotNull String path) throws Exception {
        doTest(path, KtClass.class, false);
    }

    public void doClassTestWithJava(@NotNull String path) throws Exception {
        doTest(path, KtClass.class, true);
    }

    public void doJavaClassTest(@NotNull String path) throws Exception {
        doTest(path, PsiClass.class, true);
    }

    public void doObjectTest(@NotNull String path) throws Exception {
        doTest(path, KtObjectDeclaration.class, false);
    }

    public void doFunctionTest(@NotNull String path) throws Exception {
        doTest(path, KtFunction.class, false);
    }

    public void doFunctionTestWithJava(@NotNull String path) throws Exception {
        doTest(path, KtFunction.class, true);
    }

    public void doJavaMethodTest(@NotNull String path) throws Exception {
        doTest(path, PsiMethod.class, true);
    }
    
    public void doJavaParameterTest(@NotNull String path) throws Exception {
        doTest(path, PsiParameter.class, true);
    }

    public void doPropertyTest(@NotNull String path) throws Exception {
        doTest(path, KtProperty.class, false);
    }

    public void doPropertyTestWithJava(@NotNull String path) throws Exception {
        doTest(path, KtProperty.class, true);
    }

    public void doJavaPropertyTest(@NotNull String path) throws Exception {
        doTest(path, PsiMethod.class, true);
    }

    public void doTypeAliasTest(@NotNull String path) throws Exception {
        doTest(path, KtTypeAlias.class, false);
    }

    public void doTypeParameterTest(@NotNull String path) throws Exception {
        doTest(path, KtTypeParameter.class, false);
    }

    public void doTypeParameterTestWithJava(@NotNull String path) throws Exception {
        doTest(path, KtTypeParameter.class, true);
    }

    public void doValueParameterTest(@NotNull String path) throws Exception {
        doTest(path, KtParameter.class, false);
    }

    public void doValueParameterTestWithJava(@NotNull String path) throws Exception {
        doTest(path, KtParameter.class, true);
    }

    private <T extends PsiElement> void doTest(
            @NotNull String path, @NotNull Class<T> elementClass, boolean withJava) throws Exception {
        String[] filePaths;
        if (withJava) {
            filePaths = new String[]{path, path.endsWith(".java") ? path.replace(".java", ".kt") : path.replace(".kt", ".java")};
        }
        else {
            filePaths = new String[]{path};
        }

        Editor[] editors = new Editor[filePaths.length];
        int i = 0;
        for (String filePath : filePaths) {
            myFixture.configureByFile(filePath);
            editors[i++] = myFixture.getEditor();
        }

        PsiElement elementAtCaret = null;
        for (Editor editor : editors) {
            elementAtCaret = TargetElementUtil.findTargetElement(
                    editor, TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED | TargetElementUtil.ELEMENT_NAME_ACCEPTED
            );
            if (elementAtCaret != null) break;
        }

        assertNotNull("Couldn't find element at caret position", elementAtCaret);

        T element = PsiTreeUtil.getParentOfType(elementAtCaret, elementClass, false);

        IgnoreTests.INSTANCE.runTestIfNotDisabledByFileDirective(
          dataFile().toPath(),
          IgnoreTests.DIRECTIVES.of(getPluginMode()),
          __ -> Collections.emptyList(),
          IgnoreTests.DirectivePosition.LAST_LINE_IN_FILE,
          __ -> {
              return KotlinLightCodeInsightFixtureTestCaseKt.withCustomCompilerOptions(myFixture.getFile().getText(), getProject(), getModule(), () -> {
                  try {
                      SafeDeleteHandler.invoke(getProject(), new PsiElement[] {element}, null, true, null);
                      for (int j = 0; j < filePaths.length; j++) {
                          File file = new File(filePaths[j] + ".after");
                          if (isFirPlugin()) {
                              File firSpecific = new File(filePaths[j] + ".fir.after");
                              if (firSpecific.exists()) {
                                  file = firSpecific;
                              }
                          }
                          assertTrue("After file is expected to be present", file.exists());
                          String actualText = editors[j].getDocument().getText();
                          String expectedText = FileUtil.loadFile(file);
                          if (!StringUtil.equals(expectedText, actualText)) {
                              //do not create missed files
                              String message = "Text mismatch in file: " + file.getName();
                              throw new FileComparisonFailedError(message, expectedText, actualText, FileUtil.toSystemIndependentName(file.getPath()));
                          }
                      }
                  } catch (BaseRefactoringProcessor.ConflictsInTestsException e) {
                      List<String> messages = new ArrayList<>(e.getMessages());
                      Collections.sort(messages);

                      File messageFile = new File(path + ".messages");
                      assertSameLinesWithFile(messageFile.getAbsolutePath(), StringUtil.join(messages, "\n"));
                  } catch (IOException e) {
                      throw new AssertionError(e);
                  }
                  return Unit.INSTANCE;
              });
          }
        );
    }
}
