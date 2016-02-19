package net.nemerosa.versioning.gradle.tasks

import net.nemerosa.versioning.gradle.VersionExtension
import org.gradle.api.DefaultTask

abstract class AbstractVersionTask extends DefaultTask {

    protected VersionExtension getVersioning() {
        return project.extensions.getByName('versioning') as VersionExtension
    }

}
