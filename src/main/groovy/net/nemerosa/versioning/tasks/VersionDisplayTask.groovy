package net.nemerosa.versioning.tasks

import net.nemerosa.versioning.VersionInfo
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class VersionDisplayTask extends DefaultTask {

    /**
     * Sets a description
     */
    VersionDisplayTask() {
        group = "Versioning"
        description = "Writes version information on the standard output."
    }

    @TaskAction
    void run() {
        // Gets the version info
        def info = project.versioning.info as VersionInfo
        // Displays the info
        if (info == VersionInfo.NONE) {
            println "[version] No version can be computed from the SCM."
        } else {
            println "[version] scm         = ${info.scm}"
            println "[version] branch      = ${info.branch}"
            println "[version] branchType  = ${info.branchType}"
            println "[version] branchId    = ${info.branchId}"
            println "[version] commit      = ${info.commit}"
            println "[version] full        = ${info.full}"
            println "[version] base        = ${info.base}"
            println "[version] build       = ${info.build}"
            println "[version] display     = ${info.display}"
            println "[version] tag         = ${info.tag ?: ''}"
            println "[version] lastTag     = ${info.lastTag ?: ''}"
            println "[version] dirty       = ${info.dirty}"
            println "[version] versionCode = ${info.versionCode}"
        }
    }

}
