package net.nemerosa.versioning.support

import org.gradle.api.GradleException

class DirtyException extends GradleException {

    DirtyException() {
        super('Dirty working copy - cannot compute version.')
    }
}
