pipeline {
    agent none
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
                        label "shazam"
                    }
                    steps {
                        sh "mvn clean verify"
                    }
                }
            }
        }
    }
}
