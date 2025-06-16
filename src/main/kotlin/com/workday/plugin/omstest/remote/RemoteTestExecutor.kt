package com.workday.plugin.omstest.remote

import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.execution.ui.RunContentManager
import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.workday.plugin.omstest.LastTestStorage


object RemoteTestExecutor {

    fun runRemoteTestClass(project: Project,
                           fqTestName: String,
                           category: String,
                           label: String) {
        runRemoteTest(
            project,
            fqTestName,
            """empty $fqTestName empty empty $category /usr/local/workday-oms/logs/junit""",
            label
        )
    }

    fun runRemoteTestMethod(project: Project, fqTestName: String, label: String) {
        runRemoteTest(
            project,
            fqTestName,
            """$fqTestName empty empty empty OMSBI /usr/local/workday-oms/logs/junit""",
            label
        )
    }

    private fun runRemoteTest(
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
        label: String
    ) {
        LastTestStorage.setRemote(host, fqTestName, jmxParams, label)

        val consoleView = TextConsoleBuilderFactory.getInstance()
            .createBuilder(project).console

        val descriptor = RunContentDescriptor(
            consoleView,
            null,
            consoleView.component,
            label
        )

        RunContentManager.getInstance(project)
            .showRunContent(DefaultRunExecutor.getRunExecutorInstance(), descriptor)

        val jmxInput = """
            open localhost:12016
            domain com.workday.oms
            bean name=JunitTestListener
            run executeTestSuite $jmxParams
        """.trimIndent().replace("\n", "\\n")

        val sshCommand = buildSshCommand(host, jmxInput)
        val scpCommand = buildScpCommand(host)

        val notification = notifyUser(project)
        ApplicationManager.getApplication().executeOnPooledThread {
            runCommand(sshCommand, consoleView, "Running test on $host")
            runCommand(scpCommand, consoleView, "Fetching logs from $host")
            notification.expire()
        }
    }

    private fun buildSshCommand(host: String, jmxInput: String): String = """
        ssh -o StrictHostKeyChecking=accept-new root@$host \
        "docker exec ots-17-17 mkdir -p /usr/local/workday-oms/logs/junit && \
        echo -e \"$jmxInput\" | java -jar /usr/local/bin/jmxterm-1.0-SNAPSHOT-uber.jar"
    """.trimIndent()

    private fun buildScpCommand(host: String): String {
        val downloadDir = "${System.getProperty("user.home")}/Downloads/"
        return "scp root@$host:/data/workdaydevqa/suv/suvots/logs/junit/* $downloadDir"
    }

    private fun notifyUser(project: Project): Notification {
        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup("OmsTest Notifications")
            .createNotification("Running remote test...", NotificationType.INFORMATION)
        notification.notify(project)
        return notification
    }

    private fun runCommand(command: String, console: ConsoleView, title: String) {
        console.print("\n> $title\n", ConsoleViewContentType.SYSTEM_OUTPUT)
        try {
            val process = ProcessBuilder("/bin/sh", "-c", command)
                .redirectErrorStream(true)
                .start()

            process.inputStream.bufferedReader().lines().forEach { line ->
                console.print("$line\n", ConsoleViewContentType.NORMAL_OUTPUT)
            }

            val exitCode = process.waitFor()
            console.print("Process exited with code $exitCode\n", ConsoleViewContentType.SYSTEM_OUTPUT)
        } catch (e: Exception) {
            console.print("Error: ${e.message}\n", ConsoleViewContentType.ERROR_OUTPUT)
        }
    }
}