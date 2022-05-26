/**
 * Plug-in development
 */

plugins {
    /**
     * Java plugin & Groovy
     */
    `java-gradle-plugin`
    groovy
    id("com.gradle.plugin-publish") version "0.15.0"
    /**
     * Versioning applied to itself
     */
    id("net.nemerosa.versioning") version "2.14.0"
    /**
     * Release in GitHub
     */
    id("com.github.breadmoirai.github-release") version "2.2.12"
}

/**
 * Meta information
 */

group = "net.nemerosa"
version = versioning.info.display

/**
 * Dependencies
 */

val jgitVersion: String by project

repositories {
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())
    implementation("org.ajoberstar.grgit:grgit-core:4.0.1")
    implementation("org.eclipse.jgit:org.eclipse.jgit.ui:${jgitVersion}")
    implementation("org.eclipse.jgit:org.eclipse.jgit:${jgitVersion}")
    implementation("org.tmatesoft.svnkit:svnkit:1.10.6")

    testImplementation("junit:junit:4.13.2")
    testImplementation("commons-lang:commons-lang:2.6")
    testImplementation("commons-io:commons-io:2.11.0")
}

/**
 * Plug-in definition
 */

pluginBundle {
    website = "https://github.com/nemerosa/versioning/"
    vcsUrl = "https://github.com/nemerosa/versioning/"
    description = "Gradle plug-in that computes version information from the SCM"
    tags = listOf("gradle", "plugin", "scm", "git", "svn", "version")
}

gradlePlugin {
    plugins {
        create("versioningPlugin") {
            id = "net.nemerosa.versioning"
            displayName = "Versioning plugin for Gradle"
            description = "Gradle plug-in that computes version information from the SCM"
            implementationClass = "net.nemerosa.versioning.VersioningPlugin"
        }
    }
}

tasks.test {
    environment("GIT_TEST_BRANCH", "feature/456-cute")
    environment("SVN_TEST_BRANCH", "feature-456-cute")
}

/**
 * GitHub release parameters
 */

val gitHubToken: String by project
val gitHubOwner: String by project
val gitHubRepo: String by project
val gitHubCommit: String by project

githubRelease {
    token(gitHubToken)
    owner(gitHubOwner)
    repo(gitHubRepo)
    tagName(version.toString())
    releaseName(version.toString())
    targetCommitish(gitHubCommit)
    overwrite(true)
}
