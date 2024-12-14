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

    echo "Authenticating with ECR in region: ${awsRegion}"
    sh """
    apt-get update && apt-get install -y unzip curl
    curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
    unzip awscliv2.zip
    sudo ./aws/install
    aws --version
    aws ecr get-login-password --region ${awsRegion} | docker login --username AWS --password-stdin ${accountid}.dkr.ecr.${awsRegion}.amazonaws.com
    """

    // Stage: Build Docker Image
    echo "Building Docker image: ${ecrRepoName}:${imageTag}"
    sh """
    docker build -t ${ecrRepoName}:${imageTag} -f ${dockerFilePath} ${contextPath}
    """

    // Stage: Tag Docker Image
    echo "Tagging Docker image: ${ecrRepoName}:${imageTag}"
    sh """
    docker tag ${ecrRepoName}:${imageTag} ${accountid}.dkr.ecr.${awsRegion}.amazonaws.com/${ecrRepoName}:${imageTag}
    """

    // Stage: Push to ECR
    echo "Pushing Docker image to ECR: ${ecrRepoName}:${imageTag}"
    sh """
    docker push ${accountid}.dkr.ecr.${awsRegion}.amazonaws.com/${ecrRepoName}:${imageTag}
    """
}
