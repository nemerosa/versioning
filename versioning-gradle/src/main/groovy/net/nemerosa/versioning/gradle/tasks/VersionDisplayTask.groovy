package net.nemerosa.versioning.gradle.tasks

import net.nemerosa.versioning.core.VersionInfo
import org.gradle.api.tasks.TaskAction

class VersionDisplayTask extends AbstractVersionTask {

    @TaskAction
    void run() {
        // Gets the version info
        def info = versioning.info
        // Displays the info
        if (info == VersionInfo.NONE) {
            println "[version] No version can be computed from the SCM."
        } else {
            println "[version] scm        = ${info.scm}"
            println "[version] branch     = ${info.branch}"
            println "[version] branchType = ${info.branchType}"
            println "[version] branchId   = ${info.branchId}"
            println "[version] commit     = ${info.commit}"
            println "[version] full       = ${info.full}"
            println "[version] base       = ${info.base}"
            println "[version] build      = ${info.build}"
            println "[version] display    = ${info.display}"
        }
    }

}
