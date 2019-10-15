node ('master') {
    stage('CONFIRM BUILD') {
        echo "You have selected QA Deployment"
        timeout(30) {
            input message: "You have selected the QA Deployment. Click proceed to initiate the QA Deployment?"
        }
    }
    stage('git Checkout') {
        checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'd4c11305-d565-4fcd-b368-9c3afaa418ca', url: 'https://github.com/Riant901/HCSC_Demo.git']]]
    }
    stage('QA Deployment') {
        sh '''
        pwd
        ws="/var/lib/jenkins/workspace/Demo/Deployment"
        cd $ws/qa_stage
        echo "Download the artifacts for Deployment"
        wget --user=admin --password=Db7Xu8Sd7Bd6Gr -r --no-parent -nH --cut-dirs=2 "https://jfroguser.jfrog.io/jfroguser/hcsc_qa/"
        echo "Download Completed"
        echo "Connect to Deployment Servers"
        phase="qa"
        unzip output.zip
        echo "Application Instance Stopped"
        cp -rf $ws/qa_stage/output/*.war $ws/qa/webapps/
        cp -rf $ws/qa_stage/output/$phase.config $ws/qa/configs/
        echo "Application Instance Started" 
        echo "Deployment Completed"
        '''
    }
    stage('Liquibase DB Deployment') {
        println "DB Update started"
        
        println "DB Updated Completed"
    }
    stage('Application URL Check') {
        def b="false"
        def n=0
        while(n<5){
            def status=sh returnStdout: true, script: 'echo $(curl -k -i https://jfroguser.jfrog.io/jfroguser/hcsc_qa/)'
            if(status.contains("200")){
                b="true"
                break
                println "Application URL is Up"
            }
            n++
        }

    }
    stage('Promote Artifacts for UAT Deployment') {
       sh '''
       echo "Promoting the Artifacts to UAT started"
            curl -X POST -u admin:Db7Xu8Sd7Bd6Gr "https://jfroguser.jfrog.io/jfroguser/api/copy/hcsc_qa/output.zip?to=/hcsc_uat/output.zip"
       echo "Promoting the Artifcats to UAT Completed"
            '''
        }
    stage('Email Notification') {
            /*string var="<hr> <head><style>table {border-collapse: collapse;width: 100%; font-family: Calibri (Body)," +
            " Helvetica, sans-serif;}th, td {text-align: left; padding: 8px;font-size: 11px;} tr:nth-child(even)" +
            "{background-color: #f2f2f2} th { background-color: #4CAF50;color: white;}th, td { border: 1px solid #ddd;}h3," +
            "th{ text-transform: uppercase;}</style></head></br><table><tr><th colspan='2'>HCSC Deployment</th></tr><tr>" +
            " <td><strong>PROJECT NAME</strong></td><td>${JOB_NAME}</td></tr><tr><td><strong>BUILD LABEL</strong></td><td>output.zip" +
            "</td></tr><tr><td><strong>SERVERS DEPLOYED</strong></td><td>Deployed on Dev</td></tr><tr><td><strong>STATUS</strong>" +
            "</td><td>${currentBuild.result}" +
            "</td></tr><tr><td><strong>Started By</strong></td><td>Admin</td></tr></table>"
            mail (
                bcc: '',
                body: "$var",
                from: 'Jenkins <qa@hcsc.com>',
                replyTo: '',
                subject: "Job '${JOB_NAME}' (${BUILD_NUMBER})",
                to: 'riant.raman@infosys.com',
                mimeType:'text/html'
            )*/
        }
    stage('Trigger UAT Deployment') { 
        build job: 'UAT_Deploy', wait : false
    }

}