import org.jetbrains.changelog.markdownToHTML

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.7.10"
    // Gradle IntelliJ Plugin
    id("org.jetbrains.intellij") version "1.8.1"
    // Gradle Changelog Plugin
    id("org.jetbrains.changelog") version "1.3.1"
    // Gradle Qodana Plugin
    id("org.jetbrains.qodana") version "0.1.13"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

// Configure project's dependencies
repositories {
    mavenCentral()
}
dependencies {
    // https://kotlinlang.org/docs/releases.html#release-details
    /*
    We cannot upgrade the ktor dependencies.

    This is because, otherwise, the program throws with:

    Exception in thread "DefaultDispatcher-worker-1 @coroutine#110" java.lang.NoSuchMethodError: 'kotlinx.coroutines.CoroutineDispatcher kotlinx.coroutines.CoroutineDispatcher.limitedParallelism(int)'
        at io.ktor.client.utils.CoroutineDispatcherUtilsKt.clientDispatcher(CoroutineDispatcherUtils.kt:22)
        at io.ktor.client.engine.cio.CIOEngine$dispatcher$2.invoke(CIOEngine.kt:28)
        at io.ktor.client.engine.cio.CIOEngine$dispatcher$2.invoke(CIOEngine.kt:27)
    ...

    This, apparently, is because the IntelliJ platform 2022.2 bundles with kotlinx.coroutines 1.5.2 while CoroutineDispatcher.limitedParallelism(), needed by newer versions of Ktor, was introduced in 1.6.0
    * https://www.jetbrains.com/legal/third-party-software/?product=IIC&version=2022.2
    * https://github.com/Kotlin/kotlinx.coroutines/releases/tag/1.6.0
    * https://mvnrepository.com/artifact/io.ktor/ktor-client-core/2.0.0-beta-1
    * https://mvnrepository.com/artifact/io.ktor/ktor-client-core/2.0.0-rc-1

    Related commits: 1440aad88dd4a00d7afbecd578bd2d6b5c8de1b2, b159cd0abbbd04dd86d9180907f6b01a7ab69135
     */
    implementation("io.ktor:ktor-client-content-negotiation:2.0.0-beta-1")
    implementation("io.ktor:ktor-client-core:2.0.0-beta-1")
    implementation("io.ktor:ktor-client-cio:2.0.0-beta-1")
    implementation("io.ktor:ktor-client-gson:2.0.0-beta-1")
    implementation("io.ktor:ktor-serialization-gson:2.2.1")
}

// Set the JVM language level used to compile sources and generate files - Java 11 is required since 2020.3
kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

// Configure Gradle IntelliJ Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(properties("pluginVersion"))
    groups.set(emptyList())
    path.set(File(projectDir, "../CHANGELOG.md").absolutePath)
}

// Configure Gradle Qodana Plugin - read more: https://github.com/JetBrains/gradle-qodana-plugin
qodana {
    cachePath.set(projectDir.resolve(".qodana").canonicalPath)
    reportPath.set(projectDir.resolve("build/reports/inspections").canonicalPath)
    saveReport.set(true)
    showReport.set(System.getenv("QODANA_SHOW_REPORT")?.toBoolean() ?: false)
}

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion")
    }

    patchPluginXml {
        version.set(properties("pluginVersion"))
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription.set(
            projectDir.resolve("../README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").run { markdownToHTML(this) }
        )

        // Get the latest available change notes from the changelog file
        changeNotes.set(provider {
            changelog.run {
                getOrNull(properties("pluginVersion")) ?: getLatest()
            }.toHTML()
        })
    }

    // Configure UI tests plugin
    // Read more: https://github.com/JetBrains/intellij-ui-test-robot
    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
        systemProperty("jb.consents.confirmation.enabled", "false")
    }

    // Exclude Kotlin packages from the plugin in order to use the ones packed in IntelliJ platform 2022.1.
    // Note: to see list of packages included, go to folder: build/idea-sandbox/plugins/Rossynt/lib
    //
    // References:
    // https://youtrack.jetbrains.com/issue/IDEA-285839
    // https://youtrack.jetbrains.com/issue/KTIJ-20529
    //
    buildPlugin {
        exclude {
            it.name.startsWith("kotlinx-coroutines-") || it.name.startsWith("kotlin-stdlib-") || it.name.startsWith("kotlin-reflect-")
        }
    }
    prepareSandbox {
        exclude {
            it.name.startsWith("kotlinx-coroutines-") || it.name.startsWith("kotlin-stdlib-") || it.name.startsWith("kotlin-reflect-")
        }
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("PUBLISH_TOKEN"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels.set(listOf(properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()))
    }
}
