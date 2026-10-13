
pipeline {
    agent any

    stages {
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
