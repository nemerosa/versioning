/**
 * Pipeline generation script.
 *
 * The Seed plug-in will give the following parameters to this script, available directly as variables:
 *
 * - raw parameters (seed generator input + scm branch)
 *   - PROJECT - raw project name, like nemerosa/seed in GitHub
 *   - PROJECT_CLASS
 *   - PROJECT_SCM_TYPE
 *   - PROJECT_SCM_URL
 *   - PROJECT_SCM_CREDENTIALS
 *   - BRANCH - basic branch name in the SCM, like branches/xxx in SVN
 *
 * - computed parameters:
 *   - SEED_PROJECT: project normalised name
 *   - SEED_BRANCH: branch normalised name
 *
 * The jobs are generated directly at the level of the branch seed job, so no folder needs to be created for the
 * branch itself.
 */

def release = BRANCH.startsWith('release/') as boolean

// Package job
freeStyleJob("${SEED_PROJECT}-${SEED_BRANCH}-build") {
    logRotator(numToKeep = 40)
    deliveryPipelineConfiguration('Commit', 'Package')
    jdk 'JDK7'
    scm {
        git {
            remote {
                url "git@github.com:nemerosa/versioning.git"
                branch "origin/${BRANCH}"
            }
            wipeOutWorkspace()
            localBranch "${BRANCH}"
        }
    }
    steps {
        gradle 'clean versionDisplay build --info --profile'
    }
    publishers {
        archiveJunit("**/build/test-results/*.xml")
        tasks(
                '**/*.java,**/*.groovy,**/*.xml,**/*.html,**/*.js',
                '**/target/**,**/node_modules/**,**/vendor/**',
                'FIXME', 'TODO', '@Deprecated', true
        )
        if (release) {
            buildPipelineTrigger("${SEED_PROJECT}/${SEED_PROJECT}-${SEED_BRANCH}/${SEED_PROJECT}-${SEED_BRANCH}-publish") {
                parameters {
                    gitRevision(true)
                }
            }
        }
    }
}

if (release) {
    // Publication job
    freeStyleJob("${SEED_PROJECT}-${SEED_BRANCH}-publish") {
        logRotator(numToKeep = 40)
        deliveryPipelineConfiguration('Release', 'Publish')
        jdk 'JDK7'
        scm {
            git {
                remote {
                    url "git@github.com:nemerosa/versioning.git"
                    branch "origin/${BRANCH}"
                }
                wipeOutWorkspace()
                localBranch "${BRANCH}"
            }
        }
        wrappers {
            injectPasswords()
        }
        steps {
            gradle '''\
clean versionDisplay versionFile build publishPluginToBintray --info --profile
-x test
-PBINTRAY_USER=${BINTRAY_USER}
-PBINTRAY_API_KEY=${BINTRAY_API_KEY}
'''
            // Reads the version information
            environmentVariables {
                propertiesFile 'build/version.properties'
            }
            // Tags and pushes
            shell '''\
git tag ${VERSION_DISPLAY}
git push origin ${VERSION_DISPLAY}
'''
        }
        publishers {
            tasks(
                    '**/*.java,**/*.groovy,**/*.xml,**/*.html,**/*.js',
                    '**/target/**,**/node_modules/**,**/vendor/**',
                    'FIXME', 'TODO', '@Deprecated', true
            )
        }
    }
}

// Pipeline view

deliveryPipelineView("Pipeline") {
    pipelineInstances(4)
    enableManualTriggers()
    showChangeLog()
    updateInterval(5)
    pipelines {
        component("Versioning", "${SEED_PROJECT}-${SEED_BRANCH}-build")
    }
}
