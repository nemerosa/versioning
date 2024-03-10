@Library("ontrack-jenkins-cli-pipeline@1.0.2") _

pipeline {

    agent {
        docker {
            image 'openjdk:11'
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

        stage("Setup") {
            steps {
                ontrackCliSetup(autoValidationStamps: true)
                sh '''
                    git config --global init.defaultBranch main
                '''
            }
        }

        stage("Build") {
            steps {
                sh '''
                    ./gradlew versionDisplay versionFile --stacktrace --parallel --console plain
                '''
                script {
                    // Reads version information
                    def props = readProperties(file: 'build/version.properties')
                    env.VERSION = props.VERSION_DISPLAY
                    env.GIT_COMMIT = props.VERSION_COMMIT
                    // Creates a build
                    ontrackCliBuild(name: env.BUILD_NUMBER, release: env.VERSION)
                }
                sh '''
                    ./gradlew build --stacktrace --parallel --console plain
                '''
            }
            post {
                always {
                    ontrackCliValidateTests(stamp: 'BUILD', pattern: 'build/test-results/**/*.xml')
                }
            }
        }

        stage('Release') {
            when {
                branch 'release/*'
            }
            environment {
                GITHUB = credentials('JENKINS_GITHUB_TOKEN')
            }
            steps {
                sh '''
                ./gradlew githubRelease --stacktrace --console plain \\
                  -PgitHubToken=${github-token} \\
                  -PgitHubCommit=${GIT_COMMIT}
                '''
            }
            post {
                always {
                    ontrackCliValidate(stamp: 'GITHUB.RELEASE')
                }
            }
        }

        stage('Publication') {
            when {
                branch 'release/*'
            }
            environment {
                GRADLE_PLUGINS = credentials('gradle-plugins')
            }
            steps {
                sh '''
                ./gradlew publishPlugins --stacktrace --console plain \\
                    -Pgradle.publish.key=${GRADLE_PLUGINS_USR} \\
                    -Pgradle.publish.secret=${GRADLE_PLUGINS_PSW}
                '''
            }
            post {
                always {
                    ontrackCliValidate(stamp: 'GRADLE.PLUGIN')
                }
            }
        }

    }

}
