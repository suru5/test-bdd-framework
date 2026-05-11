// ============================================================
//  BDD Selenium Framework — Jenkins Declarative Pipeline
//
//  Supports:
//    • Parameterised builds  (browser, tags, environment)
//    • Parallel multi-browser matrix
//    • Extent + Allure + TestNG HTML + Cucumber reports
//    • Screenshots archiving on failure
//    • Slack / email notifications
//    • Nightly regression schedule
// ============================================================

pipeline {

    agent any

    // ─── Trigger: nightly regression at 02:00 ──────────────
    triggers {
        cron('H 2 * * *')
    }

    // ─── Parameters ────────────────────────────────────────
    parameters {
        choice(
            name: 'BROWSER',
            choices: ['chrome', 'firefox', 'edge'],
            description: 'Browser to run tests on'
        )
        choice(
            name: 'ENVIRONMENT',
            choices: ['qa', 'staging', 'prod'],
            description: 'Target environment'
        )
        string(
            name: 'CUCUMBER_TAGS',
            defaultValue: '@smoke',
            description: 'Cucumber tag expression (e.g. @smoke, @regression, @smoke and @positive)'
        )
        choice(
            name: 'SUITE',
            choices: ['smoke', 'regression', 'parallel'],
            description: 'TestNG suite / Maven profile to run'
        )
        booleanParam(
            name: 'HEADLESS',
            defaultValue: true,
            description: 'Run browser in headless mode'
        )
        booleanParam(
            name: 'PARALLEL_MATRIX',
            defaultValue: false,
            description: 'Run parallel matrix across all three browsers'
        )
    }

    // ─── Environment / Tools ───────────────────────────────
    environment {
        JAVA_HOME       = tool name: 'JDK-17', type: 'jdk'
        MAVEN_HOME      = tool name: 'Maven-3.9', type: 'maven'
        PATH            = "${JAVA_HOME}/bin:${MAVEN_HOME}/bin:${env.PATH}"

        // Credentials stored in Jenkins Credentials Manager
        APP_BASE_URL    = credentials('APP_BASE_URL')
        APP_USERNAME    = credentials('APP_USERNAME')
        APP_PASSWORD    = credentials('APP_PASSWORD')

        // Allure results dir
        ALLURE_RESULTS  = 'target/allure-results'
        REPORTS_DIR     = 'reports'
    }

    options {
        // Keep last 20 builds
        buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '10'))
        // Timeout entire pipeline at 60 min
        timeout(time: 60, unit: 'MINUTES')
        // Coloured console output (requires AnsiColor plugin)
        ansiColor('xterm')
        // Timestamps in console
        timestamps()
        // Disable concurrent builds for same branch
        disableConcurrentBuilds()
    }

    stages {

        // ─── 1. Checkout ──────────────────────────────────
        stage('Checkout') {
            steps {
                echo '📥 Checking out source code...'
                checkout scm
                script {
                    env.GIT_COMMIT_MSG = sh(
                        script: 'git log -1 --pretty=%B',
                        returnStdout: true
                    ).trim()
                    env.GIT_AUTHOR = sh(
                        script: 'git log -1 --pretty=%an',
                        returnStdout: true
                    ).trim()
                }
                echo "Commit: ${env.GIT_COMMIT_MSG} by ${env.GIT_AUTHOR}"
            }
        }

        // ─── 2. Build / Compile ───────────────────────────
        stage('Build') {
            steps {
                echo '🔨 Compiling project...'
                sh '''
                    mvn clean compile test-compile \
                        --no-transfer-progress \
                        -q
                '''
            }
        }

        // ─── 3a. Single-Browser Test Run ─────────────────
        stage('Run Tests — Single Browser') {
            when {
                expression { return !params.PARALLEL_MATRIX }
            }
            steps {
                echo "🧪 Running [${params.SUITE}] tests on ${params.BROWSER} | tags: ${params.CUCUMBER_TAGS}"
                sh """
                    mvn test \
                        -P ${params.SUITE} \
                        -Dbrowser=${params.BROWSER} \
                        -Dheadless=${params.HEADLESS} \
                        -Denvironment=${params.ENVIRONMENT} \
                        -Dbase.url=${APP_BASE_URL} \
                        -Dapp.username=${APP_USERNAME} \
                        -Dapp.password=${APP_PASSWORD} \
                        -Dcucumber.tags="${params.CUCUMBER_TAGS}" \
                        --no-transfer-progress
                """
            }
        }

        // ─── 3b. Parallel Matrix (all browsers) ──────────
        stage('Run Tests — Parallel Matrix') {
            when {
                expression { return params.PARALLEL_MATRIX }
            }
            parallel {
                stage('Chrome') {
                    steps {
                        echo '🟡 Running on Chrome...'
                        sh """
                            mvn test \
                                -P ${params.SUITE} \
                                -Dbrowser=chrome \
                                -Dheadless=true \
                                -Denvironment=${params.ENVIRONMENT} \
                                -Dbase.url=${APP_BASE_URL} \
                                -Dapp.username=${APP_USERNAME} \
                                -Dapp.password=${APP_PASSWORD} \
                                -Dcucumber.tags="${params.CUCUMBER_TAGS}" \
                                -Dsurefire.reportsDirectory=target/surefire-reports-chrome \
                                --no-transfer-progress
                        """
                    }
                }
                stage('Firefox') {
                    steps {
                        echo '🦊 Running on Firefox...'
                        sh """
                            mvn test \
                                -P ${params.SUITE} \
                                -Dbrowser=firefox \
                                -Dheadless=true \
                                -Denvironment=${params.ENVIRONMENT} \
                                -Dbase.url=${APP_BASE_URL} \
                                -Dapp.username=${APP_USERNAME} \
                                -Dapp.password=${APP_PASSWORD} \
                                -Dcucumber.tags="${params.CUCUMBER_TAGS}" \
                                -Dsurefire.reportsDirectory=target/surefire-reports-firefox \
                                --no-transfer-progress
                        """
                    }
                }
                stage('Edge') {
                    steps {
                        echo '🔵 Running on Edge...'
                        sh """
                            mvn test \
                                -P ${params.SUITE} \
                                -Dbrowser=edge \
                                -Dheadless=true \
                                -Denvironment=${params.ENVIRONMENT} \
                                -Dbase.url=${APP_BASE_URL} \
                                -Dapp.username=${APP_USERNAME} \
                                -Dapp.password=${APP_PASSWORD} \
                                -Dcucumber.tags="${params.CUCUMBER_TAGS}" \
                                -Dsurefire.reportsDirectory=target/surefire-reports-edge \
                                --no-transfer-progress
                        """
                    }
                }
            }
        }

    } // end stages

    // ─── Post — Reports & Notifications ──────────────────
    post {

        always {
            echo '📊 Publishing reports...'

            // TestNG HTML Report (built-in Jenkins publisher)
            step([
                $class: 'Publisher',
                reportFilenamePattern: '**/testng-results.xml'
            ])

            // Allure Report (requires Allure Jenkins Plugin)
            allure([
                includeProperties: true,
                jdk: '',
                results: [[path: 'target/allure-results']]
            ])

            // Cucumber Reports (requires Cucumber Reports Plugin)
            cucumber(
                fileIncludePattern:    '**/cucumber-report.json',
                jsonReportDirectory:   'reports/cucumber',
                reportTitle:           'BDD Automation Report',
                buildStatus:           'UNSTABLE',
                trendsLimit:           10,
                classifications: [
                    [key: 'Browser',     value: params.BROWSER],
                    [key: 'Environment', value: params.ENVIRONMENT],
                    [key: 'Tags',        value: params.CUCUMBER_TAGS]
                ]
            )

            // Archive Extent HTML report
            publishHTML([
                allowMissing:          true,
                alwaysLinkToLastBuild: true,
                keepAll:               true,
                reportDir:             'reports/extent',
                reportFiles:           '*.html',
                reportName:            'Extent Report',
                reportTitles:          'Extent Test Report'
            ])

            // Archive logs and screenshots
            archiveArtifacts(
                artifacts:     'logs/**, reports/screenshots/**',
                allowEmptyArchive: true
            )

            // Always clean workspace of large driver binaries
            sh 'rm -rf ~/.cache/selenium || true'
        }

        success {
            echo '✅ Pipeline PASSED'
            // Slack notification (requires Slack Plugin + credentials)
            slackSend(
                channel:  '#qa-automation',
                color:    'good',
                message:  """✅ *PASSED* — ${env.JOB_NAME} #${env.BUILD_NUMBER}
Browser: ${params.BROWSER} | Env: ${params.ENVIRONMENT} | Tags: ${params.CUCUMBER_TAGS}
<${env.BUILD_URL}|View Build> | <${env.BUILD_URL}allure|Allure Report>"""
            )
        }

        failure {
            echo '❌ Pipeline FAILED'
            slackSend(
                channel:  '#qa-automation',
                color:    'danger',
                message:  """❌ *FAILED* — ${env.JOB_NAME} #${env.BUILD_NUMBER}
Browser: ${params.BROWSER} | Env: ${params.ENVIRONMENT} | Tags: ${params.CUCUMBER_TAGS}
<${env.BUILD_URL}|View Build> | <${env.BUILD_URL}allure|Allure Report>"""
            )
            // Email on failure (requires Email Extension Plugin)
            emailext(
                subject: "❌ TEST FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body:    """
                    <h2>Build Failed</h2>
                    <p><b>Job:</b> ${env.JOB_NAME} #${env.BUILD_NUMBER}</p>
                    <p><b>Browser:</b> ${params.BROWSER}</p>
                    <p><b>Environment:</b> ${params.ENVIRONMENT}</p>
                    <p><b>Tags:</b> ${params.CUCUMBER_TAGS}</p>
                    <p><b>Commit:</b> ${env.GIT_COMMIT_MSG}</p>
                    <p><a href="${env.BUILD_URL}">View Build</a> |
                       <a href="${env.BUILD_URL}allure">Allure Report</a></p>
                """,
                to:      'qa-team@yourcompany.com',
                mimeType: 'text/html'
            )
        }

        unstable {
            echo '⚠️ Pipeline UNSTABLE (some tests failed)'
            slackSend(
                channel: '#qa-automation',
                color:   'warning',
                message: """⚠️ *UNSTABLE* — ${env.JOB_NAME} #${env.BUILD_NUMBER}
Some tests failed. <${env.BUILD_URL}allure|View Allure Report>"""
            )
        }

        cleanup {
            echo '🧹 Cleaning workspace...'
            cleanWs(
                cleanWhenSuccess:  false,
                cleanWhenFailure:  false,
                cleanWhenAborted:  true,
                deleteDirs:        true,
                patterns: [[pattern: 'target/', type: 'INCLUDE']]
            )
        }
    }
}
