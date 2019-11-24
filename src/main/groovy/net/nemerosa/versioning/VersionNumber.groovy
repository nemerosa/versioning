package net.nemerosa.versioning

/**
 * Representation of a version number.
 *
 * This is generally a combination of three digits with an optional qualifier.
 * These digits are major, minor and path numbers.
 *
 * This representation is one of many interpretation of version number format. It is let to developer's appreciation.
 */
class VersionNumber {
    /**
     * Major version
     */
    final int major

    /**
     * Minor version
     */
    final int minor

    /**
     * Patch version
     */
    final int patch

    /**
     * Optional qualifier
     */
    final String qualifier

    /**
     * version code
     */
    final int versionCode

    /**
     * Original full version string
     */
    final String versionString

    VersionNumber(int major, int minor, int patch, String qualifier, int versionCode, String versionString) {
        this.major = major
        this.minor = minor
        this.patch = patch
        this.qualifier = qualifier
        this.versionCode = versionCode
        this.versionString = versionString
    }
}
