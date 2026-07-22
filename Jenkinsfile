// Windows-compatible Jenkinsfile

pipeline {

    agent any

    triggers { githubPush() }

    tools {
        jdk 'jdk21'
        maven 'maven3'
    }

    environment {
        BASE_URL = 'https://www.naukri.com'
    }

    parameters {
        string(name: 'CUCUMBER_TAG', defaultValue: '@Job_01', description: 'Cucumber tag to execute')
    }

    stages {

        stage('Clean Workspace') {
            steps {
                deleteDir()
            }
        }

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Execute Tests') {
            steps {
                script {
                    withCredentials([
                        usernamePassword(credentialsId: 'naukri-login',
                        usernameVariable: 'NAUKRI_EMAIL',
                        passwordVariable: 'NAUKRI_PASSWORD')
                    ]) {

                        bat '''
echo ===========================
echo JAVA OPTIONS TEST
echo ===========================
mvn -version
echo NAUKRI_EMAIL=%NAUKRI_EMAIL%
mvn help:evaluate -Dexpression=NAUKRI_URL -DNAUKRI_URL=https://www.naukri.com -q -DforceStdout
'''

                        bat """
mvn clean test ^
-Dtest=CucumberTestRunner ^
-Dcucumber.filter.tags=${params.CUCUMBER_TAG} ^
-DNAUKRI_URL=https://www.naukri.com ^
-DNAUKRI_EMAIL=%NAUKRI_EMAIL% ^
-DNAUKRI_PASSWORD=%NAUKRI_PASSWORD% ^
-Dbrowser.headless=true ^
-f pom.xml
"""
                    }
                }
            }
        }

        stage('Generate Cucumber HTML Report') {
            steps { echo 'Generating report' }
        }

        stage('Zip Cucumber HTML Report') {
            steps {
                powershell '''
$reportPath="target\\cucumber-reports\\cucumber-html-reports"
$zipPath="target\\cucumber-reports\\cucumber-html-reports.zip"
if(Test-Path $reportPath){
 if(Test-Path $zipPath){Remove-Item $zipPath -Force}
 Compress-Archive -Path "$reportPath\\*" -DestinationPath $zipPath -Force
}
'''
            }
        }
    }

    post {
        always {

            junit allowEmptyResults: true,
                  testResults: 'target/surefire-reports/*.xml'

            publishHTML(target: [
                allowMissing: false,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/cucumber-reports/cucumber-html-reports',
                reportFiles: 'overview-features.html',
                reportName: 'Cucumber HTML Report'
            ])

            script {

                def buildStatus = currentBuild.currentResult
                def headerColor = "#2ecc71"
                if(buildStatus=="UNSTABLE"){ headerColor="#f1c40f" }
                if(buildStatus=="FAILURE"){ headerColor="#e74c3c" }

                catchError(buildResult:'SUCCESS', stageResult:'SUCCESS'){
                    powershell '''
$reportPath="target\\cucumber-reports\\cucumber-html-reports"
$zipPath="target\\cucumber-reports\\cucumber-html-reports.zip"
if(Test-Path $reportPath){
 if(Test-Path $zipPath){Remove-Item $zipPath -Force}
 Compress-Archive -Path "$reportPath\\*" -DestinationPath $zipPath -Force
}
'''
                }

                withCredentials([
                    usernamePassword(credentialsId:'gmail_cred',
                    usernameVariable:'SMTP_USER',
                    passwordVariable:'SMTP_PASS')
                ]) {

                    bat """
mvn exec:java ^
-Dexec.mainClass=com.test.robust.mail.MailTrigger ^
-Dbuild.status=${buildStatus} ^
-Dbuild.url=${env.BUILD_URL} ^
-Dheader.color=${headerColor}
"""
                }
            }
        }
    }
}
