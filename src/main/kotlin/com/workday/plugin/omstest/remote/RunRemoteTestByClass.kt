package com.workday.plugin.omstest.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.workday.plugin.omstest.remote.RemoteTestExecutor

class RunRemoteTestByClass : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR) ?: return
        val file = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE) ?: return

        val offset = editor.caretModel.offset
        val elementAtCaret: PsiElement = file.findElementAt(offset) ?: return

        val psiClass = PsiTreeUtil.getParentOfType(elementAtCaret, PsiClass::class.java) ?: return
        val className = psiClass.qualifiedName ?: return
        val tag = extractTagAnnotationValue(psiClass) ?: return
        val category = tag.split('.').last()
        val label = psiClass.name ?: "Run Test"
        RemoteTestExecutor.runRemoteTestClass(
            project,
            className,
            category,
            label
        )
    }

    private fun extractTagAnnotationValue(psiClass: PsiClass): String? {
        return psiClass.annotations
            .firstOrNull { it.qualifiedName == "org.junit.jupiter.api.Tag" }
            ?.let { tagAnnotation ->
                val valueAttr = tagAnnotation.findAttributeValue("value") ?: return null
                valueAttr.text.removeSurrounding("\"")
            }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null &&
                e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR) != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT  // Or EDT depending on UI interaction
    }
}