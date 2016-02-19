package net.nemerosa.versioning.gradle.tasks

import org.gradle.api.tasks.TaskAction

class VersionFileTask extends AbstractVersionTask {

    /**
     * File to write the information info. Defaults to <code>new File(project.buildDir, 'version.properties')</code>.
     */
    File file = new File(project.buildDir, 'version.properties')

    /**
     * Prefix to apply. Defauts to <code>VERSION_</code>
     */
    String prefix = 'VERSION_'

    @TaskAction
    void run() {
        // Gets the version info
        def info = versioning.info
        // Makes sure the parent directory exists
        def parent = file.parentFile
        if (!parent.exists()) {
            parent.mkdirs()
        }
        // Writes the info
        file.text = """\
${prefix}BUILD=${info.build}
${prefix}BRANCH=${info.branch}
${prefix}BASE=${info.base}
${prefix}BRANCHID=${info.branchId}
${prefix}BRANCHTYPE=${info.branchType}
${prefix}COMMIT=${info.commit}
${prefix}DISPLAY=${info.display}
${prefix}FULL=${info.full}
${prefix}SCM=${info.scm}
"""
    }

}
