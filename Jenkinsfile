pipeline {
    agent none
    tools {
        maven 'main'
        jdk11 'nain'
    }
    stages {
        stage('Run Tests') {
            parallel {
                stage('Maven Build skip test') {
                    agent {
                        label "master"
                    }
                    steps {
                        sh 'mvn clean package -PskipQA'
                    }
                }
                stage('Maven run tests') {
                    agent {
                        label "fatthor"
                    }
                    steps {
                        sh "mvn clean verify"
                    }
                }
            }
        }
    }
}
