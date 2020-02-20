pipeline {
    agent any
    tools { 
        maven '/usr/share/maven' 
        jdk '/usr/lib/jvm/java-1.11.0-openjdk' 
    }
    stages {
        stage ('Initialize') {
            steps {
                sh '''
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                ''' 
            }
        }

        stage ('Build') {
            steps {
                echo 'This is a minimal pipeline.'
            }
        }
    }
}
