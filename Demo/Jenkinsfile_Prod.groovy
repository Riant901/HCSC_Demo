<<<<<<< HEAD
node ('master') {
    stage('CONFIRM BUILD') {
        echo "You have selected QA Deployment"
        timeout(30) {
            input message: "You have selected the PROD Deployment. Click proceed to initiate the QA Deployment?"
        }
    }
    stage('git Checkout') {
        checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '50f925e4-7148-4360-a279-5eef0b326aef', url: 'https://github.com/Riant901/demo.git']]]
    }
    stage('UAT Deployment') {
        sh '''
        pwd
        ws="/var/lib/jenkins/workspace/Demo/Deployment"
        cd $ws/Prod_stage
        echo "Download the artifacts for Deployment"
        wget --user=admin --password=Db7Xu8Sd7Bd6Gr -r --no-parent -nH --cut-dirs=2 "https://jfroguser.jfrog.io/jfroguser/hcsc_release/"
        echo "Download Completed"
        echo "Connect to Deployment Servers"
        phase="Prod"
        unzip output.zip
        echo "Application Instance Stopped"
        cp -rf $ws/Prod_stage/output/*.war $ws/PROD/webapps/
        cp -rf $ws/Prod_stage/output/$phase.config $ws/PROD/configs/
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
            def status=sh returnStdout: true, script: 'echo $(curl -k -i https://jfroguser.jfrog.io/jfroguser/hcsc_release/)'
            if(status.contains("200")){
                b="true"
                break
                println "Application URL is Up"
            }
            n++
        }

    }
  /*stage('Promote Artifacts for Release Deployment') {
       sh '''
       echo "Promoting the Artifacts to Release started"
            curl -X POST -u admin:Db7Xu8Sd7Bd6Gr "https://jfroguser.jfrog.io/jfroguser/api/copy/hcsc_uat/output.zip?to=/hcsc_release/output.zip"
       echo "Promoting the Artifcats to Release Completed"
            '''
        }*/
    /*stage('Create Change in ServiceNow') {
       def response = serviceNow_createChange serviceNowConfiguration: [instance: 'dev74004', producerId: 'ddb36c9c-c428-40b7-9840-0e32870593e1'],credentialsId: 'admin',shortDescription:'Deploy to Test',type:'Standard',category:'Other',impact:'3- Low',risk:'Moderate',priority:'4 - Low',ci:'AS400'
        sh '''
        curl "https://dev74004.service-now.com/api/sn_chg_rest/v1/change/normal?no_such_field=something&description=HCSC&short_description=PROD_Deployment" \
        --request POST \
        --header "Accept:application/json" \
        --user 'admin':'8HncCfWhGd4Y'
            ''' 
        }*/
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
                from: 'Jenkins <PROD@hcsc.com>',
                replyTo: '',
                subject: "Job '${JOB_NAME}' (${BUILD_NUMBER})",
                to: 'riant.raman@infosys.com',
                mimeType:'text/html'
            )*/
        }

=======
node ('master') {
    stage('CONFIRM BUILD') {
        echo "You have selected QA Deployment"
        timeout(30) {
            input message: "You have selected the PROD Deployment. Click proceed to initiate the QA Deployment?"
        }
    }
    stage('git Checkout') {
        checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '50f925e4-7148-4360-a279-5eef0b326aef', url: 'https://github.com/Riant901/demo.git']]]
    }
    stage('UAT Deployment') {
        sh '''
        pwd
        ws="/var/lib/jenkins/workspace/Demo/Deployment"
        cd $ws/Prod_stage
        echo "Download the artifacts for Deployment"
        wget --user=admin --password=Db7Xu8Sd7Bd6Gr -r --no-parent -nH --cut-dirs=2 "https://jfroguser.jfrog.io/jfroguser/hcsc_release/"
        echo "Download Completed"
        echo "Connect to Deployment Servers"
        phase="Prod"
        unzip output.zip
        echo "Application Instance Stopped"
        cp -rf $ws/Prod_stage/output/*.war $ws/PROD/webapps/
        cp -rf $ws/Prod_stage/output/$phase.config $ws/PROD/configs/
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
            def status=sh returnStdout: true, script: 'echo $(curl -k -i https://jfroguser.jfrog.io/jfroguser/hcsc_release/)'
            if(status.contains("200")){
                b="true"
                break
                println "Application URL is Up"
            }
            n++
        }

    }
  /*stage('Promote Artifacts for Release Deployment') {
       sh '''
       echo "Promoting the Artifacts to Release started"
            curl -X POST -u admin:Db7Xu8Sd7Bd6Gr "https://jfroguser.jfrog.io/jfroguser/api/copy/hcsc_uat/output.zip?to=/hcsc_release/output.zip"
       echo "Promoting the Artifcats to Release Completed"
            '''
        }*/
    /*stage('Create Change in ServiceNow') {
       def response = serviceNow_createChange serviceNowConfiguration: [instance: 'dev74004', producerId: 'ddb36c9c-c428-40b7-9840-0e32870593e1'],credentialsId: 'admin',shortDescription:'Deploy to Test',type:'Standard',category:'Other',impact:'3- Low',risk:'Moderate',priority:'4 - Low',ci:'AS400'
        sh '''
        curl "https://dev74004.service-now.com/api/sn_chg_rest/v1/change/normal?no_such_field=something&description=HCSC&short_description=PROD_Deployment" \
        --request POST \
        --header "Accept:application/json" \
        --user 'admin':'8HncCfWhGd4Y'
            ''' 
        }*/
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
                from: 'Jenkins <PROD@hcsc.com>',
                replyTo: '',
                subject: "Job '${JOB_NAME}' (${BUILD_NUMBER})",
                to: 'riant.raman@infosys.com',
                mimeType:'text/html'
            )*/
        }

>>>>>>> 94b16d2401e0323304c2edf2bb8607c6b5e7fa49
}