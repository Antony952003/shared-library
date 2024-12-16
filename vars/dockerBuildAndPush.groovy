def call(Map config = [:]) {
    if (!config.ecrRegistry || !config.ecrRepo || !config.imageTag || !config.resourcesPath) {
        error "Missing required parameters for dockerBuildAndPush"
    }

    container('docker') {
        script {
            sh """
            echo "Logging into Docker with ECR password..."
            docker login --username AWS --password-stdin ${config.ecrRegistry} < /workspace/ecr-login-password.txt

            echo "Building Docker image..."
            docker build -t ${config.ecrRepo}:${config.imageTag} ${config.resourcesPath}

            echo "Tagging Docker image for ECR..."
            docker tag ${config.ecrRepo}:${config.imageTag} ${config.ecrRegistry}/${config.ecrRepo}:${config.imageTag}

            echo "Pushing Docker image to AWS ECR..."
            docker push ${config.ecrRegistry}/${config.ecrRepo}:${config.imageTag}
            """
        }
    }
}
