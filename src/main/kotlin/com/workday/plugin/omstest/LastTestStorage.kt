package com.workday.plugin.omstest


/**
 * Storage for the last executed test command and its label.
 * Used to enable re-running the last test without needing to reconfigure the command.
 *
 * @author alexander.aizikivsky
 * @since Jun-2025
 */
object LastTestStorage {
    enum class Environment {
        LOCAL, REMOTE
    }

    // Either local or remote
    var environment: Environment? = null

    // Local
    var lastCommand: List<String>? = null
    var label: String? = null

    // Remote
    var host: String? = null
    var fqTestName: String? = null
    var jmxParams: String? = null

    fun setLocal(command: List<String>, label: String) {
        this.environment = Environment.LOCAL
        this.lastCommand = command
        this.label = label

        // Clear remote-related fields
        this.host = null
        this.fqTestName = null
        this.jmxParams = null
    }

    fun setRemote(host: String, fqTestName: String, params: String, label: String) {
        this.environment = Environment.REMOTE
        this.host = host
        this.fqTestName = fqTestName
        this.jmxParams = params
        this.label = label

        // Clear local-related fields
        this.lastCommand = null
    }
}