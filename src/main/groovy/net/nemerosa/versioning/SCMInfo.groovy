package net.nemerosa.versioning

import groovy.transform.Canonical
import org.ajoberstar.grgit.Status

import java.time.ZonedDateTime

@Canonical
class SCMInfo {

    static final SCMInfo NONE = new SCMInfo()

    String branch
    String commit
    String abbreviated
    ZonedDateTime dateTime
    String tag
    String lastTag
    Object status
    boolean dirty
    boolean shallow

}
