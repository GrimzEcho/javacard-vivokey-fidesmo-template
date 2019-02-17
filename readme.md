A Gradle-powered template for developing Java Card applets with Vivokey and Fidesmo

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
<!-- END doctoc --> 

- [Overview](#overview)
- [Features](#features)
- [Setup](#setup)
- [IntelliJ Setup](#intellij-setup)
- [Structure](#structure)
- [Use](#use)
- [Command/Task reference](#commandtask-reference)
    - [Tasks that are exclusively called by other tasks.](#tasks-that-are-exclusively-called-by-other-tasks)
- [Known bugs](#known-bugs)
    - [cardinfo](#cardinfo)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->


## Overview
[VivoKey](https://www.vivokey.com/) is a line of cryptobiotic implants and their associated software. Their leading implant uses a Java Card 3.03 compatible chip that is provisioned with security and domain software from [Fidesmo](https://www.fidesmo.com/). Fidesmo acts as a sort of app store for Javacard devices, allowing applets to be distributed among a variety of hardware.

This project is a Java/Gradle based template for designing, testing, and installing JavaCard applications to Fidesmo and VivoKey devices.

## Features
  * Cross-platform - tested on Windows and Linux, but should work on MacOS as well.
  * IDE independent - Works great with IntelliJ, but can also be used with just a text editor and the command line
  * Contains all required dependencies
  * Easily configurable via the *config.gradle* file
  * Robust testing with JUnit5 and JCardSim
  * Comes with a sample applet and sample client demonstrating the fundamentals of JavaCard programming
  * Incorporates Fidesmo's [FDSM](https://github.com/fidesmo/fdsm) and [ant-javacard](https://github.com/fidesmo/fdsm) tools for building and deployment


## Setup
  1. Install Java JDK 1.8. Newer Java versions are not supported by the cap tool.
  2. Download or clone the repository
  3. Open *gradle.properties* and set `java.home` to the location of the JDK 8 folder
  4. Open *settings.gradle* and give the project a name
  5. Open *config.gradle* and adjust any locations or settings. At a minimum, you will need to supply an `appId` and `appKey` which can be obtained from [Fidesmo's developer portal](https://developer.fidesmo.com/admin)

## IntelliJ Setup
IntelliJ has great support for Gradle, but the mapping between a Gradle project and an IntelliJ project can be brittle. Below are some steps to set or reset your IntelliJ project integration.

  1. Download and install [Gradle](https://gradle.org/install/) (optional, but recommended)
  2. Select *New --> Project from Existing Sources* and locate the template directory
  3. Choose *Import project from external model* and select *Gradle*
  4. Enable *Use auto-import*, disable *Create directories for empty content roots automatically*
  5. Select *Use default Gradle wrapper*
    1. If you encounter issues, you may need to change this to *Use local grade distribution* and set the grade home and JVM locations
  6. Finish

IntelliJ will now create Idea modules for each Gradle source-set (*applet*, *client*, and *test*). You can verify or change this with *File --> Project Structure --> Modules. If you look at the dependencies section, you should see all of the required libraries for each module.

If you encounter issues with IntelliJ not correctly getting all of the Gradle dependencies, you can run `gradlew collectDependencies` to copy all of the dependencies in the *lib* folder, and then import them manually into IntelliJ.

## Structure
| Name  | Description   |
|---|---|
| /src/applet  | The JavaCard applet that will be compiled to a cap file  |
| /src/client  | A Java-based client to test the installed applet via a connected PC/SC card reader  |
| /src/test  | JUnit5 unit tests. Uses JCardSim to work directly with the applet's .class files (instead of the cap file) |
| /build/reports | Test result reports  |
| /build/classes | Generated class files for the applet, client, and tests   |
| /bin  | Standalone executable files such as FDSM.jar  |
| /gradle  | Configuration for the Gradle wrapper  |
| /jc_sdks  | JavaCard SDKs for 2.2.2, 3.03., 3.0.4, and 3.0.5. Collected from [javacard-libraries](https://github.com/martinpaljak/javacard-libraries)  |
|  /lib | Java, JavaCard, and testing libraries   |
| build.gradle | The main build file with various tasks and stated dependencies. |
| config.gradle   | Template configuration settings for path locations, applet building, and Java Card versioning  |
| gradle.properties   | Pre-Gradle settings, such as setting the JAVA_HOME directory   |
| settings.gradle   | Pre-project settings, such as the root project name   |
| gradlew   | The Gradle Wrapper executable. Used to run all task commands   |


## Use
Tasks can be run with either the Gradle Wrapper (`gradlew`) or the system's installed Gradle executable (`gradle`). Use of the wrapper is recommended as it isolates the template from any breaking changes that new versions of Gradle might introduce.

The typical  naming convention of tasks is to use camel-casing for grouping. E.g. *compileApplet*, or *runClient*

The general format of a command or task is
```text
gradlew <task>
```

Most tasks do not have any arguments, but when they do, you must use the meta-argument `gradlew <command> --args="task_args_to_pass"` for any arguments you want to pass to the tas. 

All task dependencies are contained within the tasks themselves, so you never need to run tasks in a specific order. However, the general workflow is typically:
* Edit applet source
* Create/update test sources
* `gradlew test`
* `gradlew installUpload`
* `gradlew runClient`

If a task fails, try running `gradlew clean`.

## Command/Task reference

| Command or Task | Description |
| ----------------- | -------------- |
| tasks | List all available tasks (both inherited and explicit) |
| clean | Delete the bin, lib, build, and out directories (if they exist), reset test results, and do other inherited cleanup functions. If another task fails, run `clean` as the first trouble-shooting step  
| dependencies  | Download and list both explicit and inherited dependencies  |
| collectDependencies  | Copy all dependencies to the /{lib} directory |
| javaVersion  | Prints the JDK version being used by the project. Must be JDK 1.8.   |
| test  | Uses the JUnit5 test runner to run unit tests. The task is configured to run all tests with each invocation, even those that have previously passed. Test results are outputted to the console, and a full HTML report is saved to /{build}/reports/  |
| runClient | Form: `runClient --args="<app_id_or_aid> [reset]"` Runs the smart card client application. Must supply either the full AID or the Fidesmo applet ID. May also supply an optional `reset` argument to reset the access counter to 0  |
| cap  | Convert the applet into a cap file. Relevant options are configured in the `applet`  section of *config.gradle*   |
| upload   | Generate a cap file and upload it to Fidesmo   |
| install | Generate a cap file and install it to a local card. The cap file must have previously been uploaded to Fidesmo  |
| installUpload   | Generate a cap file, upload it to Fidesmo, then install it to a card  |
| installTrace   | Generate and install a cap file, but trace all APDU commands and Fidesmo API calls  |
| uninstall   | Uninstall the indicated applet from a local card. A cap file will be generated, but the contents are ignored. Only the app-id controls which applet is removed  |
| cardinfo   | Print details about the first detected smart card and a list of installed applets  |
| installVivokeyOtp   | Installs the VivoKey OTP applet. Requires that target card is a VivoKey device   |
| uninstallVivokeyOtp   | Uninstalls the VivoKey OTP applet. Requires that the target card is a VivoKey device  |
| foo | Prints a famous quote by VivoKey's creator |

&nbsp;
#### Tasks that are exclusively called by other tasks.

These can be run manually, but there is little reason to do so.

| Task | Description |
| ----------------- | -------------- |
| setup | Create the lib, bin, and out directories. Does *not* need to be manually tun |
| setupAntJavacard   | Creates a Gradle-Ant taskdef for the ant-javacard tool. Does not need to be manually run  |
| compileApplet  | Compiles the applet source files into class files. Does not convert to cap. Does not need to be manually run  |
| compileClient  | Compiles the client source files to class files. Does not need to be manually run  |


## Known bugs

#### cardinfo
The underlying FDSM utility does not list any installed applets on VivoKey devices.