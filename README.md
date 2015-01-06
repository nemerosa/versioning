versioning
==========

Gradle plug-in to generate version information from the SCM branch.

## Applying the plug-in

The `versioning` plug-in is hosted in [JCenter](https://bintray.com/bintray/jcenter).

```groovy
buildscript {
   repositories {
      jcenter()
   }
   dependencies {
      classpath 'net.nemerosa:versioning:1.0.0'
   }
}

apply plugin: 'net.nemerosa.versioning'
```

## Using the versioning info

For example, to set the project's version using the SCM:

```groovy
allprojects {
   version = versioning.info.display
}
```

## Versioning info

Once the `versioning` plug-in has been applied, a `versioning` extension is available for the project.

Getting the read-only `ìnfo` provides access to the following information, computed from the SCM information:

Property | Description | Git: `master` | Git: `feature/great` | Git: `release/2.0`
---|---|---|---|---
`scm` | SCM source | `git` | `git` | `git`
`branch` | Branch name | `master` | `feature/great` | `release/2.0`
`branchType` | Type of branch | `master` | `feature` | `release`
`branchId` | Branch as an identifier | `master` | `feature-great` | `release-2.0`
`commit` | Full commit hash | `09ef6297deb065f14704f9987301ee6620493f70` | `09ef6297deb065f14704f9987301ee6620493f70` | `09ef6297deb065f14704f9987301ee6620493f70`
`build` | Short commit/revision indicator, suitable for a build number | `09ef629` | `09ef629` | `09ef629`
`full` | Branch ID and build | `master-09ef629` | `feature-great-09ef629` | `release-2.0-09ef629`
`base` | Base version for the display version | `` | `great` | `2.0`
`display` | Display version | `master` | `great` | `2.0.0`, `2.0.1`, ...

### Display version

The `display` version is equal to the `base` property is available or to the branch identifier.

For branches to type `release`, an additional computation occurs:

* if no tag is available on the branch which has the `base` as a prefix, the `display` version is the `base` version, suffixed with `.0`
* if a tag is available on the branch which has the `base` as a prefix, the `display` version is this tag, where the last digit is incremented by 1

By using the `display` version when tagging a release, the `display` version will be automatically incremented, patch after patch, using the `release` base at a prefix.

## Tasks

The `versioning` plug-in provides two tasks.

### `versionDisplay`

Displays the version information in the standard output. For example:

```bash
> ./gradlew versionDisplay
:versionDisplay
[version] scm        = git
[version] branch     = release/0.3
[version] branchType = release
[version] branchId   = release-0.3
[version] commit     = da50c50567073d3d3a7756829926a9590f2644c6
[version] full       = release-0.3-da50c50
[version] base       = 0.3
[version] build      = da50c50
[version] display    = 0.3.0
```

### `versionFile`

Creates a file which contains the version information. By default, the file is created at _build/version.properties_ and contains the following information:

``` bash
> ./gradlew versionFile
> cat build/version.properties
VERSION_BUILD = da50c50
VERSION_BRANCH = release/0.3
VERSION_BASE = 0.3
VERSION_BRANCHID = release-0.3
VERSION_BRANCHTYPE = release
VERSION_COMMIT = da50c50567073d3d3a7756829926a9590f2644c6
VERSION_DISPLAY = 0.3.0
VERSION_FULL = release-0.3-da50c50
VERSION_SCM = git
```

The `versionFile` task can be customised with two properties. The defaults are given below:

```groovy
versionFile {
   // Path to the file to be written
   file = new File(project.buildDir, 'version.properties')
   // Prefix to apply to the properties
   prefix = 'VERSION_'
}
```

## Customisation

The collection of the versioning info can be customised by setting some properties in the `versioning` extension.

The default properties are shown below:

```groovy
versioning {
   /**
    * Defines the SCM to use in order to collect information.
    *
    * At the moment, only Git is supported.
    */
   scm = 'git'
   /**
    * Computation of the branch type and the base, by parsing the branch name.
    * By default, we use "/" as a separator between the type and the base. If not
    * present, the type is the branch and the base is empty.
    */
    branchParser = { String branch ->
        int pos = branch.indexOf('/')
        if (pos > 0) {
            new BranchInfo(
               type: branch.substring(0, pos),
               base: branch.substring(pos + 1))
        } else {
            new BranchInfo(type: branch, base: '')
        }
    }
    /**
     * Computation of the full version
     */
    full = { branchId, abbreviated -> "${branchId}-${abbreviated}" }
    /**
     * Set of eligible branch types for computing a display version from the branch base name
     */
    releases = ['release']
}
```

## Release

The CI and release jobs are available in the [Nemerosa Jenkins](https://jenkins.nemerosa.net/job/versioning/).

See http://plugins.gradle.org/submit for the publication on the [Gradle Plug-in Portal](https://plugins.gradle.org/).
