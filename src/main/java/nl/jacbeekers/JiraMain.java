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
import java.util.ArrayList;

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
        logger.info(jiraCall.getResultCode());
        logger.info(jiraCall.getResultMessage());

        if (!Constants.OK.equals(jiraCall.getResultCode())) {
            return;
        }

        System.out.println("Query for issue DQIM-11591...");
        //Test environment
        ArrayList<String> fields = new ArrayList<String>();
        fields.add("status");
        fields.add("summary");
        jiraCall.queryJiraForIssue("DQIM-11591", fields); // null = all fields
        logger.info(jiraCall.getResultCode());
        logger.info(jiraCall.getResultMessage());

        System.out.println("Creating an issue for project DQIM...");
        String projectName ="DQIM";
        jiraCall.setProjectName(projectName);
        if(jiraCall.projectExists()) {
            System.out.println("Project >" + projectName +"< exists.");
            String issueTypeId = "14500";
            jiraCall.setIssueTypeId(issueTypeId);
                //"issuetype":{"self":"https:\/\/jira.bb8-ta.aws.abnamro.org\/rest\/api\/2\/issuetype\/14500","id":"14500","description":"Represents a data attribute (source or consumer)","iconUrl":"https:\/\/jira.bb8-ta.aws.abnamro.org\/secure\/viewavatar?size=xsmall&avatarId=44864&avatarType=issuetype","name":"Data Attribute","subtask":false,"avatarId":44864}
                //issueType
                //id=14500
                //description=Represents a data attribute (source or consumer)
                //name=Data Attribute

        } else {
            System.out.println("Project >" + projectName +"< not found. You might not have access to it or an HTTP Error occurred.");
        }



        /*
        //PR environment
        System.out.println("Query for issue DQIM-11683...");
        jiraCall.queryJiraForIssue("DQIM-11683", fields); // all fields
        logger.info(jiraCall.getResultCode());
        logger.info(jiraCall.getResultMessage());
*/
    }
}
