package nl.jacbeekers;

import com.google.gson.Gson;
import nl.jacbeekers.jira.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class JiraManageIssue {
    private Logging logging = new Logging(Logger.getLogger(JiraManageIssue.class.getName()));
    private JiraConnectivity jiraConnectivity = new JiraConnectivity();
    private String proxyHostname =null;
    private int proxyPortnumber =0;
    private CloseableHttpClient httpClient = null;
    private String projectName = null;
    private nl.jacbeekers.jira.IssueType issueType = null;
    private String issueTypeId;
    private String issueTypeName;
    private String summary;
    private String description;
    private String ReportingDepartmentName;
    private CreatedIssueResponse createdIssueResponse;

    public JiraManageIssue() {
        setProxyHostname(null);
    }

    public JiraManageIssue(String proxy, int port) {
        setProxyHostname(proxy);
        setProxyPortnumber(port);
    }

    public int createIssue() {
        String procName = "createIssue";
        int code=HttpStatus.SC_INTERNAL_SERVER_ERROR;

        getLogging().logDebug(procName, "Start");
        if( null == getJiraConnectivity().getHttpClient()) {
            getLogging().logError(Constants.CREATEISSUE_FAILED, procName + " - Failed to get an HTTPClient.");
        } else {
            getLogging().logDebug(procName,"Got an HTTPClient.");
            code = createIssuePostRequest();
            if( code == HttpStatus.SC_CREATED) {
                getLogging().logDebug(procName, "Issue created.");
            }


            try {
                getJiraConnectivity().getHttpClient().close();
            } catch(IOException e) {
                getLogging().logWarning(procName + " - Could not close HTTP Connection.");
            }
        }

        return code;
    }

    private int createIssuePostRequest() {
        String procName = "createIssuePostRequest";
        CloseableHttpResponse httpResponse;
        JiraConnectivity jiraConnectivity = getJiraConnectivity();
        String completeQueryURL = jiraConnectivity.getQueryURL() +"/issue/";
        Issue issue = new Issue();
        Fields fields = new Fields();
        issue.setFields(fields);
        fields.setIssuetype(new IssueType(getIssueTypeId(), getIssueTypeName()));
        fields.setProject(new Project());
        fields.getProject().setKey(getProjectName());

        fields.setSummary(getSummary());
        fields.description ="This issue can be deleted. It is just an IDQ tryout for the Jira API";
        ReportingDepartment reportingDepartment = new ReportingDepartment();
        reportingDepartment.setValue("Risk Management");
        fields.customfield_21200 = reportingDepartment;

        getLogging().logDebug(procName, "issue type is >" + getIssueTypeName() +"<.");
//        issue.getFields().setIssuetype(new nl.jacbeekers.jira.IssueType(getIssueType().getId(), getIssueType().getName()));

        Gson gson = new Gson();
        String body = gson.toJson(issue);

        httpResponse = getJiraConnectivity().doPost(completeQueryURL, body);
        if(null == httpResponse) {
            getLogging().logError(Constants.CREATEISSUE_FAILED, "getJiraConnectivity.doPost returned >null<.");
            return HttpStatus.SC_NO_CONTENT;
        }

        int code = getJiraConnectivity().processHttpResponse(httpResponse);
        switch (code) {
            case HttpStatus.SC_OK:
                break;
            case HttpStatus.SC_NOT_FOUND:
                break;
            case HttpStatus.SC_CREATED:
                getLogging().logDebug(procName, "Issue created.");
                setCreatedIssueResponse(retrieveCreatedIssueResponse(httpResponse));
                break;
            default:
                getLogging().logError(Constants.CREATEISSUE_FAILED, "Create issue returned HTTP error >" + code + "<.");
                break;
        }

        try {
            httpResponse.close();
        } catch(IOException e) {
            getLogging().logWarning("Could not close create issue response. Exception: " + e.toString());
        }

        return code;
    }

    public CreatedIssueResponse retrieveCreatedIssueResponse(CloseableHttpResponse httpResponse) {
        CreatedIssueResponse createdIssueResponse = null;
        //{"id":"2772602","key":"DQIM-11597","self":"https://jira.bb8-ta.aws.abnamro.org/rest/api/latest/issue/2772602"}
        HttpEntity httpEntity = httpResponse.getEntity();
        try {
            String resultString = EntityUtils.toString(httpEntity);
            Gson gson = new Gson();
            getLogging().logDebug("Converting response into Java object...");
            createdIssueResponse = gson.fromJson(resultString, CreatedIssueResponse.class);
        } catch(IOException e) {
            getLogging().logError(Constants.CREATEISSUE_FAILED, "Issue created, but response could not be interpreted. Exception: " + e.toString());
        }

        return createdIssueResponse;
    }

    // getters and setters
    private void setLogging(Logging logging) {
        this.logging = logging;
    }
    public Logging getLogging() { return this.logging; }

    public JiraConnectivity getJiraConnectivity() {
        return jiraConnectivity;
    }

    private void setJiraConnectivity(JiraConnectivity jiraConnectivity) {
        this.jiraConnectivity = jiraConnectivity;
    }

    public String getProxyHostname() {
        return proxyHostname;
    }

    public void setProxyHostname(String proxyHostname) {
        this.proxyHostname = proxyHostname;
    }

    public int getProxyPortnumber() {
        return proxyPortnumber;
    }

    public void setProxyPortnumber(int proxyPortnumber) {
        this.proxyPortnumber = proxyPortnumber;
    }

    public CloseableHttpClient getHttpClient() {
        return this.httpClient;
    }

    public void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public nl.jacbeekers.jira.IssueType getIssueType() {
        return this.issueType;
    }

    public void setIssueType(IssueType issueType) {
        this.issueType = issueType;
    }
    public void setIssueTypeId(String id) {
        this.issueTypeId = id;
    }
    public String getIssueTypeId() {
        return this.issueTypeId;
    }
    public void setIssueTypeName(String name) {
        this.issueTypeName = name;
    }
    public String getIssueTypeName() {
        return this.issueTypeName;
    }
    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReportingDepartmentName() {
        return ReportingDepartmentName;
    }

    public void setReportingDepartmentName(String reportingDepartmentName) {
        ReportingDepartmentName = reportingDepartmentName;
    }

    public void setCreatedIssueResponse(CreatedIssueResponse createdIssueResponse) {
        this.createdIssueResponse = createdIssueResponse;
    }

    public CreatedIssueResponse getCreatedIssueResponse() {
        return createdIssueResponse;
    }
}
