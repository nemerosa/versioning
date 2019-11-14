package net.nemerosa.versioning.tasks

import net.nemerosa.versioning.VersionInfo
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class VersionFileTask extends DefaultTask {

    /**
     * File to write the information info. Defaults to <code>new File(project.buildDir, 'version.properties')</code>.
     */
    File file = new File(project.buildDir, 'version.properties')

    /**
     * Prefix to apply. Defauts to <code>VERSION_</code>
     */
    String prefix = 'VERSION_'

    /**
     * Sets a description
     */
    VersionFileTask() {
        group = "Versioning"
        description = "Writes version information into a file."
    }

    @TaskAction
    void run() {
        // Gets the version info
        def info = project.versioning.info as VersionInfo
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
${prefix}TAG=${info.tag ?: ''}
${prefix}LAST_TAG=${info.lastTag ?: ''}
${prefix}DIRTY=${info.dirty}
${prefix}VERSIONCODE=${info.versionCode}
"""
    }

}
