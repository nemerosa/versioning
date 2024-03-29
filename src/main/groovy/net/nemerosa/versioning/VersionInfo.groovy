package net.nemerosa.versioning

import groovy.transform.Canonical

/**
 * Version information generated by the plug-in.
 */
@Canonical
class VersionInfo {

    static final VersionInfo NONE = new VersionInfo()

    String scm = 'n/a'
    String branch = ''
    String branchType = ''
    String branchId = ''
    String commit = ''
    String display = ''
    String full = ''
    String base = ''
    String build = ''
    String time = null
    String tag = null
    String lastTag = null
    boolean dirty = false
    boolean shallow = false
    VersionNumber versionNumber = null

}
