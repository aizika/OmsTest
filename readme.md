<h1>
  <img src="https://raw.githubusercontent.com/aizika/OmsTest/master/src/main/resources/META-INF/GradleTest.png" alt="Plugin Icon" width="40" style="vertical-align: middle;"/>
  Gradle Test Runner IntelliJ Plugin
</h1>

This plugin enhances IntelliJ IDEA with convenient right-click actions to run Gradle tests by method, class, or package. It also includes a one-click option to re-run the last test.
It was tested very lightly, so please use it at your own risk and report any issues you find.

---

## 🔌 Download the Plugin

You can download the latest version of the plugin from the link below and install it manually in IntelliJ:

👉 [Download Plugin ZIP](https://github.com/aizika/OmsTest/releases/download/v1.0-beta/OmsTest-1.0-beta.zip)
Follow the installation instructions below to set it up in IntelliJ IDEA.

---

## ✨ Features

* ✅ Context-aware Gradle test execution:

    * Run the current test **method**
    * Run the test **class**
    * Run all tests in a **package**
* ✅ Re-run the **last test** with one click or shortcut
* ✅ Works in both the **editor** and **project view**
* ✅ Outputs results to the **Run tool window**
* ✅ Fully compatible with **IntelliJ Community Edition 2024.1+**

---

## 🖱 Context Menu Usage

Right-click inside a test file or on a package folder:

```
📂 OMS Gradle Test
│ Remote SUV
├─ ▶️ Run Remote Test (Method)     
├─ ▶️ Run Remote Test (Class)     
│ Local OMS
├─ ▶️ Run Test (Method)          
├─ ▶️ Run Test (Class)
├─ ▶️ Run Test (Package)
│---------------------
└─ 🔁 Re-Run Last Test
```
There is a limited verification of the context menu items, so they may not appear in all cases.
But it's user's responsibility to ensure they are used in the correct context.

---

## ⚙️ Gradle Task Setup

Ensure your project has a test runner task like described in the [Running tests with runTestJmx task via IntelliJ](https://oms.workday.build/omsdev/getting-started/running-server-tests-with-jmx/#running-tests-with-runtestjmx-task-via-intellij) of OMS Encyclopedia.
If you use the default Gradle test task, no additional setup is needed.

---

## 🚀 Build the Plugin

```bash
./gradlew clean buildPlugin
```

Find the plugin ZIP under:

```
build/distributions/
```

---

### 📦 Installation
1. Open **IntelliJ IDEA**
2. Go to **Settings → Plugins**
3. Click the **⚙️ (gear icon)** → **Install Plugin from Disk...**
4. Choose the downloaded `.zip` file
5. Restart IntelliJ when prompted

---

## 🔁 Re-Run Last Test

Quickly re-run the most recently executed test — local or remote — with a single click or shortcut. This is especially helpful for fast iterations during development and debugging.

A useful 🎯 **Shortcut (macOS):** `⌃⌥⌘R` (Control + Option + Command + R)

✅ **What it remembers** (per IntelliJ session):
- For **local** tests: the method, class, or package and full Gradle command.
- For **remote** tests: the fully qualified test name, parameters, and host.

No need to reselect anything — just hit the button and go.

## 🏠Local Test Running
Just right-click on a test method, class, or package in the project view or editor and select the appropriate option to run tests locally.

## 🌐 Remote Test Running

This plugin also supports running tests remotely on OMS servers via JMX. You can execute test methods, classes, or packages on a remote server and fetch the logs back to your local machine.


### 🛠 Requirements

Ensure the remote server is accessible via SSH and has the necessary JMX tools installed. The plugin uses `ssh` and `scp` commands to execute tests and retrieve logs.

### 📋 Example Workflow

1. Select a test method, class, or package.
2. Right-click and choose the appropriate "Run Remote Test" option.
3. Enter the remote host details when prompted (e.g., `i-09b8e9ee004730f12.workdaysuv.com`).
4. View the test execution logs in the **Run tool window**.

### 📝 Notes
Running remote tests requires one of the **OmsTestCategories**. The plugin takes the category from the class annotation.
Running tests on packages is not supported for remote tests. Mostly to avoid the hassle of choosing the category.

### ⚙️ Configuration

- Ensure the remote server has the following:
  - `jmxterm` installed and accessible.
  - Proper permissions to execute tests and fetch logs.
- Update the plugin's host configuration if needed.

---

## 🔧 Compatibility

* Tested on IntelliJ IDEA 2024.1+
* Tested on Community (IC); should work on Ultimate (IU)
* Tested on macOS; should work cross-platform

---

## 👨‍💻 Author

alexander.aizikivsky

Found bugs? Have ideas? Want enhancements? Open an issue or PR!
