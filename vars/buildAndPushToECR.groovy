// vars/buildAndPushToECR.groovy

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

    // Stage: Authenticate with ECR
    stage('Authenticate with ECR') {
        steps {
            script {
                sh """
                aws ecr get-login-password --region ${awsRegion} | docker login --username AWS --password-stdin ${accountid}.dkr.ecr.${awsRegion}.amazonaws.com
                """
            }
        }
    }

    // Stage: Build Docker Image
    stage('Build Docker Image') {
        steps {
            script {
                sh """
                docker build -t ${ecrRepoName}:${imageTag} -f ${dockerFilePath} ${contextPath}
                """
            }
        }
    }

    // Stage: Tag Docker Image
    stage('Tag Docker Image') {
        steps {
            script {
                sh """
                docker tag ${ecrRepoName}:${imageTag} ${accountid}.dkr.ecr.${awsRegion}.amazonaws.com/${ecrRepoName}:${imageTag}
                """
            }
        }
    }

    // Stage: Push to ECR
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
