def call(Map config) {
    // Parameters
    def awsRegion = config.awsRegion ?: 'us-east-1'
    def ecrRepoName = config.ecrRepoName
    def imageTag = config.imageTag ?: 'latest'
    def dockerFilePath = config.dockerFilePath ?: './Dockerfile'
    def contextPath = config.contextPath ?: '.'
    def accountid = config.accountid

    if (!ecrRepoName) {
        error("Parameter 'ecrRepoName' is required")
    }

    pipeline {
        agent any
        stages {

            stage('Prepare Dockerfile') {
                steps {
                    script {
                        def dockerfilePath = libraryResource('Dockerfile')
                        writeFile(file: 'Dockerfile', text: dockerfilePath)
                    }
                }
            }

            stage('Authenticate with ECR') {
                steps {
                    script {
                        // AWS ECR Login
                        sh """
                        aws ecr get-login-password --region ${awsRegion} | docker login --username AWS --password-stdin ${accountid}.dkr.ecr.${awsRegion}.amazonaws.com
                        """
                    }
                }
            }
            stage('Build Docker Image') {
                steps {
                    script {
                        sh """
                        docker build -t ${ecrRepoName}:${imageTag} -f ${dockerFilePath} ${contextPath}
                        """
                    }
                }
            }
            stage('Tag Docker Image') {
                steps {
                    script {
                        sh """
                        docker tag ${ecrRepoName}:${imageTag} <account_id>.dkr.ecr.${awsRegion}.amazonaws.com/${ecrRepoName}:${imageTag}
                        """
                    }
                }
            }
            stage('Push to ECR') {
                steps {
                    script {
                        sh """
                        docker push ${accountid}.dkr.ecr.${awsRegion}.amazonaws.com/${ecrRepoName}:${imageTag}
                        """
                    }
                }
            }
        }
    }
}
