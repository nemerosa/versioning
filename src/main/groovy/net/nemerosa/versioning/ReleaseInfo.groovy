package net.nemerosa.versioning

import groovy.transform.Canonical

/**
 * Information about the branch coming from its name.
 *
 * For instance :
 *
 * release/2.0
 * type : 'release'
 * base : '2.0'
 *
 * master
 * type : 'master'
 * base : ''
 */
@Canonical
class ReleaseInfo {

    /**
     * Type of branch.
     *
     * Could be "release", "feature" or what ever has been configured
     */
    String type

    /**
     * Base of release or feature branch. This is what comes after type in branch's name.
     */
    String base

}
