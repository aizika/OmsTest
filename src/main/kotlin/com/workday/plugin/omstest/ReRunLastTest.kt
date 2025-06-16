package com.workday.plugin.omstest

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.workday.plugin.omstest.local.LocalTestExecutor
import com.workday.plugin.omstest.remote.RemoteTestExecutor.runTestWithHost

class ReRunLastTest : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        when (LastTestStorage.environment) {
            LastTestStorage.Environment.LOCAL -> {
                val command = LastTestStorage.lastCommand
                val label = LastTestStorage.label

                if (command == null || label == null) {
                    showMissingDialog(project)
                    return
                }

                LocalTestExecutor.runCommand(command, project, label)
            }

            LastTestStorage.Environment.REMOTE -> {
                val fqTestName = LastTestStorage.fqTestName
                val jmxParams = LastTestStorage.jmxParams
                val host = LastTestStorage.host
                val label = LastTestStorage.label ?: "Run test"

                if (fqTestName == null || jmxParams == null || host == null) {
                    showMissingDialog(project)
                    return
                }

                runTestWithHost(
                    project,
                    fqTestName,
                    jmxParams,
                    host,
                    label
                )

            }

            null -> {
                showMissingDialog(project)
            }
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = LastTestStorage.environment != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    private fun showMissingDialog(project: Project) {
        Messages.showWarningDialog(
            project,
            "No previous test found to re-run.",
            "Re-Run Last Test"
        )
    }
}