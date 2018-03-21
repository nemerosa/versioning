String version = ''
String gitCommit = ''
String branchName = ''
String projectName = 'versioning'

boolean pr = false

pipeline {

    agent {
        docker {
            image 'openjdk:8'
            label 'docker'
        }
    }

    options {
        // General Jenkins job properties
        buildDiscarder(logRotator(numToKeepStr: '40'))
        // Timestamps
        timestamps()
        // No durability
        durabilityHint('PERFORMANCE_OPTIMIZED')
    }

    stages {

        stage('Setup') {
            steps {
                script {
                    branchName = ontrackBranchName(BRANCH_NAME)
                    echo "Ontrack branch name = ${branchName}"
                    pr = BRANCH_NAME ==~ 'PR-.*'
                }
                script {
                    if (pr) {
                        echo "No Ontrack setup for PR."
                    } else {
                        echo "Ontrack setup for ${branchName}"
                        ontrackBranchSetup(project: projectName, branch: branchName, script: """
                            branch.config {
                                gitBranch '${branchName}', [
                                    buildCommitLink: [
                                        id: 'git-commit-property'
                                    ]
                                ]
                            }
                        """)
                    }
                }
            }
        }

        stage('Build') {
            steps {
                sh '''\
#!/bin/bash
set -e

gradlew \\
    clean \\
    versionDisplay \\
    build \\
    --stacktrace \\
    --profile \\
    --parallel \\
    --console plain
'''
            }
        }
    }
}