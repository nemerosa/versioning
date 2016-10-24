package net.nemerosa.versioning

import groovy.transform.Canonical

@Canonical
class SCMInfo {

    static final SCMInfo NONE = new SCMInfo()

    String branch
    String commit
    String abbreviated
    String tag
    boolean dirty
    boolean shallow

}
