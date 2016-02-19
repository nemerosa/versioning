package net.nemerosa.versioning.gradle

import net.nemerosa.versioning.gradle.tasks.VersionDisplayTask
import net.nemerosa.versioning.gradle.tasks.VersionFileTask
import org.gradle.api.Plugin
import org.gradle.api.Project

@SuppressWarnings("GroovyUnusedDeclaration")
class VersioningPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        // Creates a new `versionInfo` extension
        project.extensions.create('versioning', VersionExtension, project)
        // `versionDisplay` task
        project.tasks.create('versionDisplay', VersionDisplayTask)
        // `versionFile` task
        project.tasks.create('versionFile', VersionFileTask)
    }

}
