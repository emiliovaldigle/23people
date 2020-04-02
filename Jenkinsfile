#!/usr/bin/env groovy

node {
    stage('checkout') {
        checkout scm
    }


    stage('check java') {
        sh "java -version"
    }

    stage('clean') {
        sh "chmod +x gradlew"
        sh "./gradlew clean --no-daemon"
    }
    stage('nohttp') {
        sh "./gradlew checkstyleNohttp --no-daemon"
    }


    // stage('backend tests') {
    //     try {
    //         sh "./gradlew test integrationTest jacocoTestReport"
    //     } catch(err) {
    //         throw err
    //     } finally {
    //         junit '**/build/**/TEST-*.xml'
    //     }
    //  }

    stage('packaging') {
        sh "./gradlew bootJar -x test -Pprod -PnodeInstall --no-daemon"
        archiveArtifacts artifacts: '**/build/libs/*.jar', fingerprint: true
    }


    def dockerImage
    stage('publish docker') {
        // A pre-requisite to this step is to setup authentication to the docker registry
        // https://github.com/GoogleContainerTools/jib/tree/master/jib-gradle-plugin#authentication-methods
        sh "./gradlew jibDockerBuild"
    }
    stage('up docker containers') {
        sh "docker-compose -f src/main/docker/app.yml up -d"
    }
}
