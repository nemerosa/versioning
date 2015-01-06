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
---|---
`scm` | SCM source | `git` | `git` | `git`
`branch` | Branch name | `master` | `feature/great` | `release/2.0`
`branchType` | Type of branch | `master` | `feature` | `release`
`branchId` | Branch as an identifier | `master` | `feature-great` | `release-2.0`
`commit` | Full commit hash | `09ef6297deb065f14704f9987301ee6620493f70` | `09ef6297deb065f14704f9987301ee6620493f70` | `09ef6297deb065f14704f9987301ee6620493f70`
`build` | Short commit/revision indicator, suitable for a build number | `09ef629` | `09ef629` | `09ef629`
`full` | Branch ID and build | `master-09ef629` | `feature-great-09ef629` | `release-2.0-09ef629`
`base` | Base version for the display version | `` | `great` | `2.0`
`display` | Display version | `master` | `great` | `2.0.0`, `2.0.1`, ...

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
[version] branch     = master
[version] branchType = master
[version] branchId   = master
[version] commit     = 9c542df306c3eb840588921237869a14af780fbf
[version] full       = master-9c542df
[version] base       =
[version] build      = 9c542df
[version] display    = master
```

## Release

See http://plugins.gradle.org/submit.
