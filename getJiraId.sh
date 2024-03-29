##
# Script to test the code on MacOS or Linux
# July 2021
# This script assumes the jira2relational jar and all the dependencies are in the same directory (see JIRA2RELATIONAL_DEPENDENCIES below)
##
# Notes
# * Make sure the version numbers of the jars match the versions you expect and use in your IDE.
##
# Location of the Jars
# Change the following line to match your Informatica installation
# e.g.: JIRA2RELATIONAL_DEPENDENCIES=/appl/Informatica/10.2.2/services/shared/jars/thirdparty
JIRA2RELATIONAL_DEPENDENCIES=lib
##
# Jira connectivity
##
JIRA_HOSTNAME=my-dummy-hostname-for-jira
JIRA_USERNAME=me_myself_and_i
JIRA_PASSWORD=something_really_secret
JIRA_PROXY=myproxy
JIRA_PROXY_PORT=8765
##
# Jira Key
JIRA_ISSUE_KEY=TEST-1234
##
# 
cd $JIRA2RELATIONAL_DEPENDENCIES
#
## Command line options for the code itself:
# -a,--action <arg>      Action to conduct. Has to be >query< or >create<.
# -k,--issuekey <arg>    Issue key to search for. Only if action=query
# -l,--loginurl <arg>    Jira login URL.
# -p,--password <arg>    user password
# -q,--queryurl <arg>    Jira query URL.
# -u,--user <arg>        Username
# -x,--proxy <arg>       Proxy hostname
# -y,--proxyport <arg>   Proxy port number
##
echo "Jira hostname: $JIRA_HOSTNAME"
echo "Call with depending jars"
JIRA2RELATIONAL_JAR=original-jira2relational-0.6-SNAPSHOT.jar
##
java -cp \
commons-cli-1.4.jar:\
guava-30.1.1-jre.jar:\
jira-rest-java-client-core-5.2.2.jar:\
commons-codec-1.15.jar:\
httpclient-4.5.13.jar:\
${JIRA2RELATIONAL_JAR}:\
commons-io-2.11.0.jar:\
commons-logging-1.2.jar:\
httpcore-4.4.14.jar:\
log4j-core-2.14.1.jar:\
log4j-api-2.14.1.jar:\
fugue-4.7.2.jar:\
jettison-1.4.1.jar:\
gson-2.8.7.jar:\
jira-rest-java-client-api-5.2.2.jar \
nl.jacbeekers.JiraMain \
--loginurl "https://$JIRA_HOSTNAME/rest/auth/latest/session" \
--user "$JIRA_USERNAME" \
--password "$JIRA_PASSWORD" \
--queryurl "$JIRA_HOSTNAME/rest/api/latest" \
--proxy "$JIRA_PROXY" \
--proxyport "$JIRA_PROXY_PORT" \
--action query \
--issuekey "$JIRA_ISSUE_KEY"
#
