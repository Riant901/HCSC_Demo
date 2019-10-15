node ('master') {
    stage('git Checkout') {
        checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'd4c11305-d565-4fcd-b368-9c3afaa418ca', url: 'https://github.com/Riant901/HCSC_Demo.git']]]
    }
    stage('UAT Deployment') {
        sh '''
       pwd
        ws="/var/lib/jenkins/workspace/HCSC_Dev_Build_Deploy/Deployment"
        cd $ws/stage
        echo "Download the artifacts for Deployment"
        #wget --user=admin --password=Db7Xu8Sd7Bd6Gr -r --no-parent -nH --cut-dirs=2 "https://jfroguser.jfrog.io/jfroguser/hcsc_qa/"
        echo "Download Completed"
        echo "Connect to Deployment Servers"
        phase="uat"
        #unzip hcsc_output.zip
        echo "Application Instance Stopped"
        cp -rf $ws/stage/hcsc_output/*.war $ws/stage/UAT/webapps/
        cp -rf $ws/stage/hcsc_output/$phase.config $ws/stage/UAT/configs/
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
            def status=sh returnStdout: true, script: 'echo $(curl -k -i https://jfroguser.jfrog.io/jfroguser/hcsc_uat/)'
            if(status.contains("200")){
                b="true"
                break
                println "Application URL is Up"
            }
            n++
        }

    }
    stage('Promote Artifacts for Release Deployment') {
       sh '''
       echo "Promoting the Artifacts to Release started"
            curl -X POST -u admin:Db7Xu8Sd7Bd6Gr "https://jfroguser.jfrog.io/jfroguser/api/copy/hcsc_uat/hcsc_output.zip?to=/hcsc_release/hcsc_output.zip"
       echo "Promoting the Artifcats to Release Completed"
            '''
        }
    stage('Create Change in ServiceNow') {
        sh '''
        curl "https://dev74004.service-now.com/api/sn_chg_rest/v1/change/normal?no_such_field=something&description=HCSC&short_description=PROD_Deployment" \
        --request POST \
        --header "Accept:application/json" \
        --user 'admin':'8HncCfWhGd4Y'
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
                from: 'Jenkins <UAT@hcsc.com>',
                replyTo: '',
                subject: "Job '${JOB_NAME}' (${BUILD_NUMBER})",
                to: 'riant.raman@infosys.com',
                mimeType:'text/html'
            )*/
        }
    stage('CONFIRM BUILD') {
        echo "CR created for PROD Deployment"
        timeout(30) {
            input message: "Approve/Reject. Click proceed to initiate the QA Deployment?"
        }
    }
    stage('Trigger Prod Deployment') { 
        build job: 'HCSC_Prod_Deploy', wait : false
    }

}
