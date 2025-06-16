package com.workday.plugin.omstest.remote

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil

class RunRemoteTestByMethod : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return
        val offset = editor.caretModel.offset
        val element = file.findElementAt(offset) ?: return

        val method = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java) ?: run {
            showError(project, "No test method found at cursor.")
            return
        }

        val containingClass = PsiUtil.getTopLevelClass(method) ?: run {
            showError(project, "Cannot determine the containing class.")
            return
        }
        val classFqn = containingClass.qualifiedName ?: return

        RemoteTestExecutor.runRemoteTestMethod(
            project,
            "$classFqn@${method.name}",
            "${method.containingClass?.name}.${method.name}"
        )
    }

    private fun showError(project: Project, message: String) {
        Messages.showErrorDialog(project, message, "Remote Test Error")
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT  // Or EDT depending on UI interaction
    }
}