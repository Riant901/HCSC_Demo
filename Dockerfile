FROM tomcat:7-jre7-alpine
ADD ./target/InformationCentre.war $CATALINA_HOME/webapps
EXPOSE 9090
