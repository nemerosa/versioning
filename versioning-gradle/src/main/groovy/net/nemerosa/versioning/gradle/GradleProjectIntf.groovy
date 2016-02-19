package net.nemerosa.versioning.gradle

import net.nemerosa.versioning.core.ProjectIntf
import org.gradle.api.Project

class GradleProjectIntf implements ProjectIntf {

    private final Project project

    GradleProjectIntf(Project project) {
        this.project = project
    }

    @Override
    File getRootDir() {
        return project.rootDir
    }

    @Override
    File getFile(String relativePath) {
        return project.rootProject.file(relativePath)
    }

    @Override
    void log(String pattern, Object... parameters) {
        project.logger.info("[versioning] ${String.format(pattern, parameters)}")
    }
}
