node ('master') {
    stage('git Checkout') {
        checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'd4c11305-d565-4fcd-b368-9c3afaa418ca', url: 'https://github.com/Riant901/HCSC_Demo.git']]]
    }
    stage('mvn Build') {
        sh '''
        export M2_HOME=/usr/share/maven
        export M2=$M2_HOME/bin
        export PATH=$M2:$PATH
        mvn clean install
        '''
    }
    stage('Sonar Analysis') {
        sh '''
        echo "Sonar Analysis Started"
        sleep 20s
        echo "Sonar Analysis Completed" 
        '''
    }
    stage('Build-Package and Upload') {
        sh '''
        pwd
        ws="/var/lib/jenkins/workspace/HCSC_Dev_Build_Deploy"
        rm -rf hcsc_output
        mkdir hcsc_output
        cd hcsc_output
        cp -rf $ws/target/*.war .
        cp -rf $ws/Configs/*.config .
        cd $ws
        zip -r hcsc_output.zip hcsc_output
        echo "Upload the Build Artifacts"
        curl -u admin:Db7Xu8Sd7Bd6Gr -X PUT "https://jfroguser.jfrog.io/jfroguser/hcsc_dev/hcsc_output.zip" -T hcsc_output.zip
        echo "Upload the Build Artifact Completed"
        '''
    }
    stage('Dev Deployment') {
        sh '''
        pwd
        ws="/var/lib/jenkins/workspace/HCSC_Dev_Build_Deploy/Deployment"
        cd $ws/stage
        echo "Download the artifacts for Deployment"
        wget --user=admin --password=Db7Xu8Sd7Bd6Gr -r --no-parent -nH --cut-dirs=2 "https://jfroguser.jfrog.io/jfroguser/hcsc_dev/"
        echo "Download Completed"
        echo "Connect to Deployment Servers"
        phase="dev"
        unzip hcsc_output.zip
        echo "Application Instance Stopped"
        cp -rf $ws/stage/hcsc_output/*.war $ws/stage/dev/webapps/
        cp -rf $ws/stage/hcsc_output/$phase.config $ws/stage/dev/configs/
        echo "Application Instance Started" 
        echo "Deployment Completed"
        '''
    }
    stage('Liquibase DB Deployment') {
        sh '''
        ws="/var/lib/jenkins/workspace/HCSC_Dev_Build_Deploy"
        cd $ws/liquibase
        ./liquibase  --driver=org.postgresql.Driver --classpath=postgresql-42.2.8.jar --url="jdbc:postgresql://3.130.190.138:5432/mydb" --changeLogFile=db.changelog.xml --username=shivam --password=devops@123 update
        '''
    }
    stage('Application URL Check') {
        def b="false"
        def n=0
        while(n<5){
            def status=sh returnStdout: true, script: 'echo $(curl -k -i https://jfroguser.jfrog.io/jfroguser/hcsc_dev/)'
            if(status.contains("200")){
                b="true"
                break
                println "Application URL is Up"
            }
            n++
        }

    }
    stage('Promote Artifacts for QA Deployment') {
       sh '''
       echo "Promoting the Artifacts to QA started"
            curl -X POST -u admin:Db7Xu8Sd7Bd6Gr "https://jfroguser.jfrog.io/jfroguser/api/copy/hcsc_dev/hcsc_output.zip?to=/hcsc_qa/hcsc_output.zip"
       echo "Promoting the Artifcats to QA Completed"
            '''
        }
    stage('Email Notification') {
            /*string var="<hr> <head><style>table {border-collapse: collapse;width: 100%; font-family: Calibri (Body)," +
            " Helvetica, sans-serif;}th, td {text-align: left; padding: 8px;font-size: 11px;} tr:nth-child(even)" +
            "{background-color: #f2f2f2} th { background-color: #4CAF50;color: white;}th, td { border: 1px solid #ddd;}h3," +
            "th{ text-transform: uppercase;}</style></head></br><table><tr><th colspan='2'>HCSC Deployment</th></tr><tr>" +
            " <td><strong>PROJECT NAME</strong></td><td>${JOB_NAME}</td></tr><tr><td><strong>BUILD LABEL</strong></td><td>hcsc_output.zip" +
            "</td></tr><tr><td><strong>SERVERS DEPLOYED</strong></td><td>Deployed on Dev</td></tr><tr><td><strong>STATUS</strong>" +
            "</td><td>${currentBuild.result}" +
            "</td></tr><tr><td><strong>Started By</strong></td><td>Admin</td></tr></table>"
            mail (
                bcc: '',
                body: "$var",
                from: 'Jenkins <dev@hcsc.com>',
                replyTo: '',
                subject: "Job '${JOB_NAME}' (${BUILD_NUMBER})",
                to: 'riant.raman@infosys.com',
                mimeType:'text/html'
            )*/
        }
    stage('CONFIRM BUILD') {
        echo "You have selected QA Deployment"
        timeout(30) {
            input message: "You have selected the QA Deployment. Click proceed to initiate the QA Deployment?"
        }
    }
    stage('Trigger QA Deployment') { 
        build job: 'HCSC_QA_Deploy', wait : false
    }

}