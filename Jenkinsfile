node('android-34') {
    dir(path: '/build') {
        stage('Checkout') {
            checkout scm
        }
        stage('Prepare') {
            env.BUILD_ID = sh(returnStdout: true, script: 'curl https://jenkins.lan/id-gen/cledwynl/mbga').trim()
            currentBuild.displayName = "#${env.BUILD_ID}"
        }
        stage('Check') {
            catchError(buildResult: env.BRANCH_IS_PRIMARY ? 'FAILURE' : 'UNSTABLE', stageResult: 'FAILURE') {
                sh './gradlew ktLint'
            }
        }
        stage('Build') {
            def target = env.BRANCH_IS_PRIMARY ? 'Release' : 'Feature'
            withCredentials([
                certificate(
                   credentialsId: 'keystore',
                   keystoreVariable: 'KEYSTORE',
                   passwordVariable: 'STORE_PASSWORD',
                ),
                usernamePassword(
                   credentialsId: 'key-alias',
                   usernameVariable: 'KEY_ALIAS',
                   passwordVariable: 'KEY_PASSWORD'
                )
            ]) {
                sh 'printenv'
                sh "./gradlew assemble${target}"
            }
            archiveArtifacts(artifacts: 'app/build/outputs/apk/**')
        }
    }
}
