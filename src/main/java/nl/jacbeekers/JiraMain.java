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

import org.apache.commons.cli.*;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class JiraMain {

    private static String loginURL;
    private static String username;
    private static String password;
    private static String queryURL;
    private static String action;
    private static String proxyHostname=null;
    private static int proxyPortnumber=0;
    private static String queryForIssue;
    private static org.apache.log4j.Logger logger = Logger.getLogger(JiraMain.class.getName());

    public static void usage(org.apache.log4j.Logger logger) {
        logger.info("Usage:");
        logger.info(JiraMain.class.getName() +" <loginURL> <username> <password> <queryURL> [proxyHostname] [proxyPortnumber] <action>" );
        logger.info("where:");
        logger.info("  <loginURL> is the complete login URL for Jira, including http(s), hostname, port.");
        logger.info("  <username> is the Jira username to be used.");
        logger.info("  <password> is the password of the Jira username. In this version clear text (unfortunately).");
        logger.info("  <queryURL> is the complete URL for the Jira API queries.");
        logger.info("  <action> is the action to execute. Choose query or create.");
        logger.info("     for action=query you need to supply a Jira issue id. ");
    }

    public static void main(String[] args) throws IOException, ParseException {

        // create Options object
        final Options options = new Options();
        // add loginurl option
        options.addOption(new Option("l", "loginurl", true, "Jira login URL."));
        options.addOption(new Option("q", "queryurl", true, "Jira query URL."));
        options.addOption(new Option("u", "user", true, "Username"));
        options.addOption(new Option("p", "password", true, "user password"));
        options.addOption(new Option("x", "proxy", true, "Proxy hostname"));
        options.addOption(new Option("y", "proxyport", true, "Proxy port number"));
        options.addOption(new Option("a", "action", true, "Action to conduct. Has to be >query< or >create<."));
        options.addOption(new Option("k", "issuekey", true, "Issue key to search for. Only if action=query"));

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse( options, args);

        loginURL = cmd.getOptionValue('l');
        username = cmd.getOptionValue('u');
        password = cmd.getOptionValue('p');
        queryURL = cmd.getOptionValue('q');
        action = cmd.getOptionValue('a');
        proxyHostname=cmd.getOptionValue('x');
        proxyPortnumber= Integer.parseInt(cmd.getOptionValue('y'));
        queryForIssue = cmd.getOptionValue('k');

        if(null == action | null == loginURL | null == username | null == password | null == queryURL) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "JiraMain", options );
            System.exit(3);
        }

        switch(action) {
            case "query":
                runQuery();
                break;
            case "create":
                createIssue();
                break;
            default:
                System.out.println("Invalid action specified. Use >query< or >create<.");
                System.exit(1);
        }


        /*
        //PR environment
        System.out.println("Query for issue DQIM-11683...");
        jiraCall.queryJiraForIssue("DQIM-11683", fields); // all fields
        logger.info(jiraCall.getResultCode());
        logger.info(jiraCall.getResultMessage());
*/
    }

    public static void runQuery() throws IOException {
        JiraCall jiraCall =null;
        jiraCall = new JiraCall();
        jiraCall.setQueryURL(queryURL);
        jiraCall.setLoginURL(loginURL);

            //Create HttpClient
            System.out.println("Getting http client...");
            jiraCall.createHttpClient(proxyHostname, proxyPortnumber);
            System.out.println("Logging in...");
            //login
            jiraCall.login(username, password);
            logger.info(jiraCall.getLogging().getResultCode());
            logger.info(jiraCall.getLogging().getResultMessage());

            if (!Constants.OK.equals(jiraCall.getLogging().getResultCode())) {
                return;
            }
            String projectName ="TRAIN";
            jiraCall.setProjectName(projectName);

            System.out.println("Getting issue type...");
            String issueTypeId = "14500";
            String issueTypeName ="Data Element";
            jiraCall.setIssueTypeId(issueTypeId);
            jiraCall.setIssueTypeName(issueTypeName);

            System.out.println("Query for issue >" + queryForIssue +"<...");
            //Test environment
            ArrayList<String> fields = new ArrayList<String>();
            fields.add("status");
            fields.add("summary");
            int rc = jiraCall.queryJiraForIssue(queryForIssue, fields); // null = all fields
            System.out.println("Query for Jira issue >" + queryForIssue + "< returned >" + rc + "<.");

//        jiraCall.queryJiraForIssue(queryForIssue, null); // null = all fields
//            logger.info(jiraCall.getLogging().getResultCode());
//            logger.info(jiraCall.getLogging().getResultMessage());

            /*
            System.out.println("Checking project DQIM...");
            if(jiraCall.projectExists()) {
                System.out.println("Project >" + projectName +"< exists.");
                System.out.println("Checking issue type >" + issueTypeName +"<...");
                if(jiraCall.issueTypeExists()) {
                    System.out.println("Issue type exists.");
                } else {
                    System.out.println("Issue type does not exist.");
                }
            } else {
                System.out.println("Project >" + projectName +"< not found. You might not have access to it or an HTTP Error occurred.");
            }
            */

        jiraCall.close();

    }

    public static void createIssue() throws IOException {

        JiraManageIssue jiraManagementIssue = new JiraManageIssue(proxyHostname, proxyPortnumber);
        jiraManagementIssue.getJiraConnectivity().setQueryURL(queryURL);
        jiraManagementIssue.getJiraConnectivity().setLoginURL(loginURL);
        jiraManagementIssue.getJiraConnectivity().setUsername(username);
        jiraManagementIssue.getJiraConnectivity().setPassword(password);
        jiraManagementIssue.getJiraConnectivity().setProxyHostname(proxyHostname);
        jiraManagementIssue.getJiraConnectivity().setProxyPortnumber(proxyPortnumber);
//        jiraManagementIssue.setHttpClient(jiraManagementIssue.getJiraConnectivity().getHttpClient());
        jiraManagementIssue.getJiraConnectivity().login(username, password);

        // Base info
        jiraManagementIssue.setProjectName("TRAIN");

        // Input for new issue
        jiraManagementIssue.setSummary("Summary");

        // Description
        jiraManagementIssue.setDescription("Description");

        //if CDE then high, else medium
        jiraManagementIssue.setPriorityName("High");

        //business line depends on LoGS dataset - customfield_21200
        jiraManagementIssue.setBusinessLineName("Retail Banking");

        // Reporting department name  or customfield_22100
        jiraManagementIssue.setReportingDepartmentName("Commercial Banking");

        // Assignee = Delegated Data Owner
        jiraManagementIssue.setAssigneeName("");

        // Data Element = Data Attribute - customfield_19802
        jiraManagementIssue.setDataElement("SBI code");

        // Impact Description - customfield_15702
        jiraManagementIssue.setImpactDescription("Impact: Please specify it");

        // Acceptance Criteria - customfield_10502 (Cannot be set while creating an issue)
//        jiraManagementIssue.setAcceptanceCriteria("Acceptance: Please specify it properly");

        // Country = "NL" - customfield_20200
        jiraManagementIssue.setCountry("Netherlands");

        // Data Owner - customfield unknown
        jiraManagementIssue.setDataOwner("Fred Bos");

        // Issue Type -
        jiraManagementIssue.setIssueTypeId("14500");
        jiraManagementIssue.setIssueTypeName("Data Attribute");

        // Linked issue
        jiraManagementIssue.setLinkedIssue("TRAIN-363");

       int rc = jiraManagementIssue.createIssue();
        if (rc == HttpStatus.SC_CREATED) {
            System.out.println("Issue created with id >" + jiraManagementIssue.getCreatedIssueResponse().getId()+ "< and key >"
                    + jiraManagementIssue.getCreatedIssueResponse().getKey() + "<.");
        }

    }

}
