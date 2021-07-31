##
# Script to test the code on MacOS or Linux
# July 2021
# This script assumes you've just the maven-shade-plugin to generate a jar that contains all dependencies
##
# Location of the Jars
# Change the following line to match your Informatica installation
# e.g.: JIRA2RELATIONAL_DEPENDENCIES=/appl/Informatica/10.2.2/services/shared/jars/thirdparty
JIRA2RELATIONAL_DEPENDENCIES=target
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
##
echo "Call with shaded jar (the jar contains all dependencies)"
JIRA2RELATIONAL_JAR=jira2relational-0.6-SNAPSHOT.jar
##
java -cp \
${JIRA2RELATIONAL_JAR} \
nl.jacbeekers.JiraMain \
--loginurl "https://$JIRA_HOSTNAME/rest/auth/latest/session" \
--user "$JIRA_USERNAME" \
--password "$JIRA_PASSWORD" \
--queryurl "https://$JIRA_HOSTNAME/rest/api/latest" \
--proxy "$JIRA_PROXY" \
--proxyport "$JIRA_PROXY_PORT" \
--action query \
--issuekey "$JIRA_ISSUE_KEY"
#

