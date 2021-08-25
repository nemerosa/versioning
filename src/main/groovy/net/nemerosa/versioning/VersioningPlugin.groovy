package net.nemerosa.versioning

import net.nemerosa.versioning.tasks.VersionDisplayTask
import net.nemerosa.versioning.tasks.VersionFileTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class VersioningPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        // Creates a new `versionInfo` extension
        project.extensions.create('versioning', VersioningExtension, project)
        // `versionDisplay` task
        project.tasks.register('versionDisplay', VersionDisplayTask)
        // `versionFile` task
        project.tasks.register('versionFile', VersionFileTask)
    }

}
