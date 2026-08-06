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
                // Run inside the same Maven image used by the app's Dockerfile so the
                // Jenkins host doesn't need Maven/JDK installed directly.
                sh '''
                    docker run --rm \
                        -v "$WORKSPACE/backend":/app -w /app \
                        -v jenkins_maven_repo:/root/.m2 \
                        maven:3.9.9-eclipse-temurin-17 \
                        mvn -B -q clean verify
                '''
            }
        }

        stage('Build & Test Frontend') {
            steps {
                // Same idea: use a throwaway Node container instead of requiring
                // npm/node on the Jenkins host.
                sh '''
                    docker run --rm \
                        -v "$WORKSPACE/frontend":/app -w /app \
                        node:20-alpine \
                        sh -c "npm ci && npm run build"
                '''
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
                        if curl -sf http://localhost:8083/actuator/health; then
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
