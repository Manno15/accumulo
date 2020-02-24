/*
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

pipeline {
    agent none
    tools {
        maven 'main'
        jdk 'main'
    }
    stages {
        stage('Run Tests') {
            parallel {
               
                stage('Assemble') {
                    agent {
                        label "master"
                    }
                    steps {
                        sh 'mvn clean package -PskipQA'
                        sh "mvn install -pl assemble"
                    }
                }
                stage('Server/Base') {
                    agent {
                        label "aquaman"
                    }
                    steps {
                        sh 'mvn clean package -PskipQA'
                        sh "mvn install -pl server/base"
                    }
                }
                stage('Server/Monitor') {
                    agent {
                        label "aquaman"
                    }
                    steps {
                        sh 'mvn clean package -PskipQA'
                        sh "mvn install -pl server/monitor"
                    }
                }
                stage('Server/Native') {
                    agent {
                        label "superman"
                    }
                    steps {
                        sh 'mvn clean package -PskipQA'
                        sh "mvn install -pl server/native"
                    }
                }
                stage('core') {
                    agent {
                        label "superman"
                    }
                    steps {
                        sh 'mvn clean package -PskipQA'
                        sh "mvn install -pl core"
                    }
                }
                 stage('Server/GC') {
                    agent {
                        label "superman"
                    }
                    steps {
                        sh 'mvn clean package -PskipQA'
                        sh "mvn install -pl server/gc"
                    }
                }
                stage('Hadoop-mapreduce') {
                    agent {
                        label "batman"
                    }
                    steps {
                        sh 'mvn clean package -PskipQA'
                        sh "mvn install -pl hadoop-mapreduce"
                    }
                }
                stage('Server/Master') {
                    agent {
                        label "batman"
                    }
                    steps {
                        sh 'mvn clean package -PskipQA'
                        sh "mvn install -pl server/master"
                    }
                }
                 stage('Iterator-test-harness') {
                    agent {
                        label "shazam"
                    }
                    steps {
                        sh 'mvn clean package -PskipQA'
                        sh "mvn install -pl iterator-test-harness"
                    }
                }
                stage('Server/Tracer') {
                    agent {
                        label "shazam"
                    }
                    steps {
                        sh 'mvn clean package -PskipQA'
                        sh "mvn install -pl server/tracer"
                    }
                }
                 stage('Server/Tserver') {
                    agent {
                        label "shazam"
                    }
                    steps {
                        sh 'mvn clean package -PskipQA'
                        sh "mvn install -pl server/tserver"
                    }
                }
                //  stage('Minicluster') {
                //     agent {
                //         label "nightwing"
                //     }
                //     steps {
                //         sh "mvn install -pl minicluster"
                //     }
                // }
                //  stage('Shell') {
                //     agent {
                //         label "nightwing"
                //     }
                //     steps {
                //         sh "mvn install -pl shell"
                //     }
                // }
                //  stage('Start') {
                //     agent {
                //         label "nightwing"
                //     }
                //     steps {
                //         sh "mvn install -pl start"
                //     }
                // }
                 stage('Tests') {
                    agent {
                        label "fatthor"
                    }
                    steps {
                        sh 'mvn clean package -PskipQA'
                        //sh "mvn install -pl test"
                    }
                }
            }
        }
        stage('temp step'){
            parallel{
                stage('start'){
              agent {
                        label "shazam"
                    }
                    steps {
                        sh "mvn install -pl start"
                    }
                }
                  stage('minicluster'){
              agent {
                        label "shazam"
                    }
                    steps {
                        sh "mvn install -pl minicluster"
                    }
                }
                  stage('shell'){
              agent {
                        label "shazam"
                    }
                    steps {
                        sh "mvn install -pl shell"
                    }
                }
            }
        }
    }
}
