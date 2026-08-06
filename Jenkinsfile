pipeline {
    agent any

    options {
        // Prevent overlapping deploys if two builds trigger back to back.
        disableConcurrentBuilds()
        timestamps()
    }

    environment {
        // Values are pulled from a Jenkins credentials/.env file on the host instead
        // of being hard-coded here. Adjust the credential IDs to match your Jenkins setup.
        COMPOSE_PROJECT_NAME = '108-08-portfolio-manager'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test Backend') {
            steps {
                dir('backend') {
                    sh 'mvn -B -q clean verify'
                }
            }
        }

        stage('Build & Test Frontend') {
            steps {
                dir('frontend') {
                    sh 'npm ci'
                    sh 'npm run build'
                }
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    docker compose down --remove-orphans
                    docker compose up -d --build
                '''
            }
        }

        stage('Health Check') {
            steps {
                sh '''
                    for i in $(seq 1 10); do
                        if curl -sf http://localhost:4001/actuator/health; then
                            exit 0
                        fi
                        sleep 5
                    done
                    echo "Backend did not become healthy in time" >&2
                    exit 1
                '''
            }
        }
    }

    post {
        failure {
            sh 'docker compose logs --tail=200 || true'
        }
        always {
            cleanWs()
        }
    }
}
