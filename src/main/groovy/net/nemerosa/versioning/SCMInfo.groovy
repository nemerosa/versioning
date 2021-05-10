package net.nemerosa.versioning

import groovy.transform.Canonical
import org.ajoberstar.grgit.Status

@Canonical
class SCMInfo {

    static final SCMInfo NONE = new SCMInfo()

    String branch
    String commit
    String abbreviated
    String tag
    String lastTag
    Status status
    boolean dirty
    boolean shallow

}
