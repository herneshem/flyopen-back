pipeline {
    agent any

    tools {
        jdk 'JDK 21'
    }

    stages {
        stage('Comprobar Java') {
            steps {
                bat 'echo JAVA_HOME=%JAVA_HOME%'
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

    post {
        success {
            echo 'Compilación y pruebas completadas correctamente.'
        }
        failure {
            echo 'La compilación o las pruebas han fallado.'
        }
    }
}