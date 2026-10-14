
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

        stage('Analizar en SonarCloud') {
            steps {
                withCredentials([
                    string(
                        credentialsId: 'sonarcloud-token',
                        variable: 'SONAR_TOKEN'
                    )
                ]) {
                    bat '''
                        mvnw.cmd -B org.sonarsource.scanner.maven:sonar-maven-plugin:sonar ^
                        -Dsonar.projectKey=herneshem_flyopen-back ^
                        -Dsonar.organization=herneshem ^
                        -Dsonar.host.url=https://sonarcloud.io ^
                        -Dsonar.token=%SONAR_TOKEN%
                        -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'Compilación, pruebas y análisis completados correctamente.'
        }
        failure {
            echo 'La compilación, las pruebas o el análisis han fallado.'
        }
    }
}