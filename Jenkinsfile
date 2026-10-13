
pipeline {
    agent any

    stages {
        stage('Comprobar Java') {
            steps {
                bat 'echo JAVA_HOME=%JAVA_HOME%'
                bat 'where java'
                bat 'java -version'
                bat 'mvnw.cmd -version'
            }
        }

        stage('Compilar y probar') {
            steps {
                bat 'mvnw.cmd -B clean verify'
            }
        }
    }
}
