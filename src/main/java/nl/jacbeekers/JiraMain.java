/*
 * MIT License
 *
 * Copyright (c) 2019 Jac. Beekers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package nl.jacbeekers;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class JiraMain {
    public static void usage(org.apache.log4j.Logger logger) {
        logger.info("Usage:");
        logger.info(JiraMain.class.getName() +" <loginURL> <username> <password> <queryURL> [proxyHostname] [proxyPortnumber]" );
        logger.info("where:");
        logger.info("  <loginURL> is the complete login URL for Jira, including http(s), hostname, port.");
        logger.info("  <username> is the Jira username to be used.");
        logger.info("  <password> is the password of the Jira username. In this version clear text (unfortunately).");
        logger.info("  <queryURL> is the complete URL for the Jira API queries.");
    }

    public static void main(String[] args) throws IOException {
        org.apache.log4j.Logger logger = Logger.getLogger(JiraMain.class.getName());

        if (args.length < 4) {
            usage(logger);
            return;
        }

        String loginURL = args[0];
        String username = args[1];
        String password = args[2];
        String queryURL = args[3];
        String proxyHostname=null;
        int proxyPortnumber=0;

        if(args.length > 4) {
            proxyHostname = args[4];
            proxyPortnumber = Integer.parseInt(args[5]);
        }
        JiraCall jiraCall =null;

        jiraCall = new JiraCall();

        jiraCall.setQueryURL(queryURL);
        jiraCall.setLoginURL(loginURL);

        //Create HttpClient
        System.out.println("Getting http client...");
//        jiraCall.createHttpClient();
        jiraCall.createHttpClient(proxyHostname, proxyPortnumber);
        System.out.println("Logging in...");
        //login
        jiraCall.login(username, password);
        logger.info(jiraCall.getLogging().getResultCode());
        logger.info(jiraCall.getLogging().getResultMessage());

        if (!Constants.OK.equals(jiraCall.getLogging().getResultCode())) {
            return;
        }
        String projectName ="DQIM";
        jiraCall.setProjectName(projectName);

        System.out.println("Getting issue type...");
        String issueTypeId = "14500";
        String issueTypeName ="Data Element";
        jiraCall.setIssueTypeId(issueTypeId);
        jiraCall.setIssueTypeName(issueTypeName);

        System.out.println("Query for issue DQIM-11600...");
        //Test environment
        ArrayList<String> fields = new ArrayList<String>();
        fields.add("status");
        fields.add("summary");
        //jiraCall.queryJiraForIssue("DQIM-11600", fields); // null = all fields
        jiraCall.queryJiraForIssue("DQIM-11600", null); // null = all fields
        logger.info(jiraCall.getLogging().getResultCode());
        logger.info(jiraCall.getLogging().getResultMessage());

        System.out.println("Creating an issue for project DQIM...");
        if(jiraCall.projectExists()) {
            System.out.println("Project >" + projectName +"< exists.");
//            String issueTypeId = "14500";
//            String issueTypeName ="Data Attribute";
//            jiraCall.setIssueTypeId(issueTypeId);
//            jiraCall.setIssueTypeName(issueTypeName);
            if(jiraCall.issueTypeExists()) {
                System.out.println("Issue type exists.");
            } else {
                System.out.println("Issue type does not exist.");
            }
                //"issuetype":{"self":"https:\/\/jira.bb8-ta.aws.abnamro.org\/rest\/api\/2\/issuetype\/14500","id":"14500","description":"Represents a data attribute (source or consumer)","iconUrl":"https:\/\/jira.bb8-ta.aws.abnamro.org\/secure\/viewavatar?size=xsmall&avatarId=44864&avatarType=issuetype","name":"Data Attribute","subtask":false,"avatarId":44864}
                //issueType
                //id=14500
                //description=Represents a data attribute (source or consumer)
                //name=Data Attribute

        } else {
            System.out.println("Project >" + projectName +"< not found. You might not have access to it or an HTTP Error occurred.");
        }

        // Regardless of the above outcome, create a Jira issue
        JiraManageIssue jiraManagementIssue = new JiraManageIssue(proxyHostname, proxyPortnumber);
        jiraManagementIssue.setProjectName("DQIM");

        jiraManagementIssue.setSummary("IDQ Jira API tryout");
        jiraManagementIssue.setDescription("Dummy item from IDQ for API testing purpose.");
        //if CDE then high, else medium
        jiraManagementIssue.setPriorityName("High");
        //business line depends on LoGS dataset - customfield_21200
        jiraManagementIssue.setBusinessLineName("Risk Management");
        // customfield_22111 or customfield_22100
        jiraManagementIssue.setReportingDepartmentName("Risk Management");
        // Assignee = Delegated Data Owner - customfield_20400
        jiraManagementIssue.setAssigneeName("Ton Reurts");
        // Data Element = Data Attribute - customfield_19802
        jiraManagementIssue.setDataElement("SBI code");
        // Impact Description - customfield_15702
        jiraManagementIssue.setImpactDescription("Please specify");
        // Acceptance Criteria = Axon Rule description - customfield_10502
        jiraManagementIssue.setAcceptanceCriteria("Please specify");
        // Country = "NL" - customfield_20200
        jiraManagementIssue.setCountry("NL");
        // Data Owner - customfield unknown
        jiraManagementIssue.setDataOwner("Ton Reurts");
        // Issue Type -
        jiraManagementIssue.setIssueTypeId("14500");
        jiraManagementIssue.setIssueTypeName("Data Attribute");
        // Due Date
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        System.out.println("Due date set to " + dateFormat.format(date));
        jiraManagementIssue.setDueDate(date);


        jiraManagementIssue.getJiraConnectivity().setQueryURL(queryURL);
        jiraManagementIssue.getJiraConnectivity().setLoginURL(loginURL);
        jiraManagementIssue.getJiraConnectivity().setUsername(username);
        jiraManagementIssue.getJiraConnectivity().setPassword(password);
        jiraManagementIssue.getJiraConnectivity().setProxyHostname(proxyHostname);
        jiraManagementIssue.getJiraConnectivity().setProxyPortnumber(proxyPortnumber);
        jiraManagementIssue.getJiraConnectivity().login(username, password);
//        jiraManagementIssue.setHttpClient(jiraManagementIssue.getJiraConnectivity().getHttpClient());
/*        int rc = jiraManagementIssue.createIssue();
        if (rc == HttpStatus.SC_CREATED) {
            System.out.println("Issue created with id >" + jiraManagementIssue.getCreatedIssueResponse().getId()+ "< and key >"
                    + jiraManagementIssue.getCreatedIssueResponse().getKey() + "<.");
        }

*/
        jiraCall.close();

        /*
        //PR environment
        System.out.println("Query for issue DQIM-11683...");
        jiraCall.queryJiraForIssue("DQIM-11683", fields); // all fields
        logger.info(jiraCall.getResultCode());
        logger.info(jiraCall.getResultMessage());
*/
    }
}
