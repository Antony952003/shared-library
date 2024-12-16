def call(Map config = [:]) {
    if (!config.credentialsId || !config.awsRegion || !config.ecrRegistry) {
        error("Missing required parameters for awsECRLogin")
    }

    container('aws-cli') {
        withCredentials([
            [$class: 'AmazonWebServicesCredentialsBinding', credentialsId: config.credentialsId],
            string(credentialsId: 'session-token', variable: 'AWS_SESSION_TOKEN')
        ]) {
            sh """
            export AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}
            export AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}
            export AWS_SESSION_TOKEN=${AWS_SESSION_TOKEN}
            echo "Logging into AWS ECR..."
            aws ecr get-login-password --region ${config.awsRegion} > /workspace/ecr-login-password.txt
            """
        }
    }
}
