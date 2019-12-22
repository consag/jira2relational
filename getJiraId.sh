cd /appl/Informatica/10.2.2/services/shared/jars/thirdparty
java -cp gson-2.8.6.jar:jira-rest-java-client-core-5.1.6.jar:jira-rest-java-client-api-5.1.6.jar:jira2relational-0.1-SNAPSHOT.jar:httpcore-4.4.12.jar:httpclient-4.5.10.jar:log4j-log4j-1.2.12.jar:jettison-1.4.0.jar:commons-logging-1.2.jar:commons-codec-1.13.jar:joda-time-2.10.5.jar nl.jacbeekers.JiraMain "https://<JIRAHOST>/rest/auth/latest/session" "idq" "PASSWORD" "https://<JIRAHOST>/rest/api/latest/issue" "<PROXYHOSTNAME>" "8080"

