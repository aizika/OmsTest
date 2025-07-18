package com.workday.plugin.omstest.execution

import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.execution.ui.RunContentManager
import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.workday.plugin.omstest.PluginConstants
import com.workday.plugin.omstest.ui.TestResultPresenter
import com.workday.plugin.omstest.ui.UiContentDescriptor
import com.workday.plugin.omstest.ui.UiProcessHandler
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.SystemIndependent
import java.nio.file.Files
import kotlin.io.path.Path

/**
 * Utility object for running remote tests on a specified host.
 * Provides methods to run test classes and methods with JMX parameters.
 *
 * @author alexander.aizikivsky
 * @since Jun-2025
 */
object RemoteTestExecutor {

    fun runRemoteTest(
        project: Project,
        fqTestName: String,
        jmxParams: String,
        label: String
    ) {
        val dialog = HostPromptDialog()
        if (!dialog.showAndGet()) return
        val host = dialog.getHost()

        runTestWithHost(project, fqTestName, jmxParams, host, label)
    }


    fun runTestWithHost(
        project: Project,
        fqTestName: String,
        jmxParams: String,
        host: String,
        runTabName: String
    ) {
        LastTestStorage.setRemote(host, fqTestName, jmxParams, runTabName)

        // Delete test result file if exists
        val targetDir = project.basePath ?: "."
        val fullPathLocal =
            Path("$targetDir${PluginConstants.LOCAL_RESULTS_DIR}" + "/" + PluginConstants.TEST_RESULT_FILE_NAME)
        val fullPathRemote = Path(targetDir + "/" + PluginConstants.TEST_RESULT_FILE_NAME)
        Files.deleteIfExists(fullPathLocal)
        Files.deleteIfExists(fullPathRemote)

        val descriptor = UiContentDescriptor.Companion.createDescriptor(project, runTabName)

        val processHandler = descriptor.getUiProcessHandler()
        val consoleView = descriptor.getConsoleView()
        RunContentManager.getInstance(project)
            .showRunContent(DefaultRunExecutor.getRunExecutorInstance(), descriptor)

        val sshCommand = """
                ssh -o StrictHostKeyChecking=accept-new root@$host <<'ENDSSH'
                docker exec ots-17-17 mkdir -p /usr/local/workday-oms/logs/junit
                java -jar /usr/local/bin/jmxterm-1.0-SNAPSHOT-uber.jar <<'JMX'
                open localhost:12016
                domain com.workday.oms
                bean name=JunitTestListener
                run executeTestSuite $jmxParams
                JMX
                ENDSSH
            """.trimIndent()

        val scpCommand = buildScpCommand(host, targetDir)

        val notification = notifyUser(project)

        processHandler.startNotify()
        ApplicationManager.getApplication().executeOnPooledThread {
            runRemoteCommand(sshCommand, consoleView, processHandler, "Running test on $host")
            runRemoteCommand(scpCommand, consoleView, processHandler, "Fetching logs from $host")

            notification.expire()
            TestResultPresenter().displayParsedResults(processHandler, project.basePath) {
                processHandler.finish()
            }
        }
    }

    private fun buildScpCommand(host: String, targetDir: @SystemIndependent @NonNls String): String {
        return "scp root@$host:/data/workdaydevqa/suv/suvots/logs/junit/* \"$targetDir\""
    }

    private fun notifyUser(project: Project): Notification {
        return NotificationGroupManager.getInstance()
            .getNotificationGroup("OmsTest Notifications")
            .createNotification("Running remote test...", NotificationType.INFORMATION)
            .also { it.notify(project) }
    }

    private fun runRemoteCommand(
        command: String,
        console: ConsoleView,
        processHandler: UiProcessHandler,
        title: String
    ) {
        console.print("\n> $title\n", ConsoleViewContentType.SYSTEM_OUTPUT)
        try {
            val process = ProcessBuilder("/bin/sh", "-c", command)
                .redirectErrorStream(true)
                .start()

            process.inputStream.bufferedReader().lines().forEach { line ->
                processHandler.notifyTextAvailable(line + "\n", ProcessOutputTypes.STDOUT)
            }

            val exitCode = process.waitFor()
            console.print("Process exited with code $exitCode\n", ConsoleViewContentType.SYSTEM_OUTPUT)
        } catch (e: Exception) {
            console.print("Error: ${e.message}\n", ConsoleViewContentType.ERROR_OUTPUT)
        }
    }
}