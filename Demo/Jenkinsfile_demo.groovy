node ('master') {
    stage('git Checkout') {
        checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '50f925e4-7148-4360-a279-5eef0b326aef', url: 'https://github.com/Riant901/HCSC_Demo.git']]]
    }
    stage('mvn Build') {
        sh '''
        export M2_HOME=/usr/share/maven
        export M2=$M2_HOME/bin
        export PATH=$M2:$PATH
        mvn clean install
        '''
    }
    stage('Build-Package and Upload') {
        sh '''
        pwd
        ws="/var/lib/jenkins/workspace/Demo"
        rm -rf output
        mkdir output
        cd output
        cp -rf $ws/multi-module/webapp/target/*.war .
        cp -rf $ws/Configs/*.config .
        cd $ws
        zip -r output.zip output
        echo "Upload the Build Artifacts"
        curl -u admin:Db7Xu8Sd7Bd6Gr -X PUT "https://jfroguser.jfrog.io/jfroguser/hcsc_dev/output.zip" -T output.zip
        echo "Upload the Build Artifact Completed"
        '''
    }
    stage('Dev Deployment') {
        sh '''
        pwd
        ws="/var/lib/jenkins/workspace/Demo/Deployment"
        cd $ws/stage
        echo "Download the artifacts for Deployment"
        wget --user=admin --password=Db7Xu8Sd7Bd6Gr -r --no-parent -nH --cut-dirs=2 "https://jfroguser.jfrog.io/jfroguser/hcsc_dev/"
        echo "Download Completed"
        echo "Connect to Deployment Servers"
        phase="dev"
        unzip output.zip
        echo "Application Instance Stopped"
        cp -rf $ws/stage/output/*.war $ws/dev/webapps/
        cp -rf $ws/stage/output/$phase.config $ws/dev/configs/
        echo "Application Instance Started" 
        echo "Deployment Completed"
        '''
    }
    stage('Liquibase DB Deployment') {
        sh '''
        ws="/var/lib/jenkins/workspace/Demo"
        cd $ws/liquibase
        ./liquibase  --driver=org.postgresql.Driver --classpath=postgresql-42.2.8.jar --url="jdbc:postgresql://localhost:5432/capDB" --changeLogFile=db.changelog-1.0.xml --username=shivam --password=devops@123 generateChangeLog
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
            curl -X POST -u admin:Db7Xu8Sd7Bd6Gr "https://jfroguser.jfrog.io/jfroguser/api/copy/hcsc_dev/output.zip?to=/hcsc_qa/output.zip"
       echo "Promoting the Artifcats to QA Completed"
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
                from: 'Jenkins <dev@hcsc.com>',
                replyTo: '',
                subject: "Job '${JOB_NAME}' (${BUILD_NUMBER})",
                to: 'riant.raman@infosys.com',
                mimeType:'text/html'
            )*/
        }
    stage('Trigger QA Deployment') { 
        build job: 'QA_Deploy', wait : false
    }

}