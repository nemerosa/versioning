/**
 * List of global parameters:
 *
 * - JDK7
 *
 * List of global passwords:
 *
 * - BINTRAY_USER
 * - BINTRAY_API_KEY
 *
 * List of plug-ins:
 *
 * - Delivery pipeline
 * - Build pipeline
 * - Parameterized trigger
 * - Git
 * - Folders
 * - Set build name
 */

/**
 * Variables
 */

def REPOSITORY = 'nemerosa/versioning'
def PROJECT = 'versioning'
def LOCAL_REPOSITORY = '/var/lib/jenkins/repository/versioning'

/**
 * Folder for the project (making sure)
 */

folder {
    name PROJECT
}

/**
 * Generation for all branches
 */

URL branchApi = new URL("https://api.github.com/repos/${REPOSITORY}/branches")
def branches = new groovy.json.JsonSlurper().parse(branchApi.newReader())

branches.each {
    def BRANCH = it.name
    // Branch type
    def branchType
    int pos = BRANCH.indexOf('/')
    if (pos > 0) {
        branchType = BRANCH.substring(0, pos)
    } else {
        branchType = BRANCH
    }
    println "BRANCH = ${BRANCH}"
    println "\tBranchType = ${branchType}"
    // Keeps only some version types
    if (['master', 'feature', 'release', 'hotfix'].contains(branchType)) {
        // Normalised branch name
        def NAME = BRANCH.replaceAll(/[^A-Za-z0-9\.\-_]/, '-')
        println "\tGenerating ${NAME}..."
        // Folder for the branch
        folder {
            name "${PROJECT}/${PROJECT}-${NAME}"
        }

        // Package job
        job {
            name "${PROJECT}/${PROJECT}-${NAME}/${PROJECT}-${NAME}-01-package"
            logRotator(numToKeep = 40)
            deliveryPipelineConfiguration('Commit', 'Package')
            jdk 'JDK7'
            scm {
                git {
                    remote {
                        url "git@github.com:${REPOSITORY}.git"
                        branch "origin/${BRANCH}"
                    }
                    wipeOutWorkspace()
                    localBranch "${BRANCH}"
                }
            }
            steps {
                gradle 'clean build --info --profile'
                conditionalSteps {
                    condition {
                        status('SUCCESS', 'SUCCESS')
                    }
                    runner('Fail')
                    shell """\
# Copies the JAR to a local directory
cp build/libs/*.jar ${LOCAL_REPOSITORY}
"""
                }
            }
            publishers {
                archiveJunit("**/build/test-results/*.xml")
                tasks(
                        '**/*.java,**/*.groovy,**/*.xml,**/*.html,**/*.js',
                        '**/target/**,**/node_modules/**,**/vendor/**',
                        'FIXME', 'TODO', '@Deprecated', true
                )
            }
        }

        // Pipeline view

        view(type: DeliveryPipelineView) {
            name "${PROJECT}/${PROJECT}-${NAME}/Pipeline"
            pipelineInstances(4)
            enableManualTriggers()
            showChangeLog()
            updateInterval(5)
            pipelines {
                component("versioning-${NAME}", "${PROJECT}/${PROJECT}-${NAME}/${PROJECT}-${NAME}-01-package")
            }
        }


    } else {
        println "\tSkipping ${BRANCH}."
    }
}
