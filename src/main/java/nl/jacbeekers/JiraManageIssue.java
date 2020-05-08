package nl.jacbeekers;

import com.google.gson.Gson;
import nl.jacbeekers.jira.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JiraManageIssue {
    private Logging logging = new Logging(LogManager.getLogger(JiraManageIssue.class.getName()));
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
    private Priority priority;
    private String priorityName;
    private String businessLineName;
    private Region region;
    private String regionName;
    private String ReportingDepartmentName;
    private Assignee assignee;
    private String assigneeName;
    private String dataElement;
    private String impactDescription;
    private String acceptanceCriteria;
    private String country;
    private String dataOwner;
    private Date dueDate;
    private String linkedIssue;
    private String body;

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

        // Constructing the issue
        // Code translates from provided attributes to needed Java/Jira attributes
        Issue issue = new Issue();
        Fields fields = new Fields();
        issue.setFields(fields);

        // Base attributes
        fields.setSummary(getSummary());
        fields.setDescription(getDescription());
        fields.setImpactDescription(getImpactDescription());
        fields.setAcceptanceCriteria(getAcceptanceCriteria());

        // Issue Type
        fields.setIssuetype(new IssueType(getIssueTypeId(), getIssueTypeName()));

        // Project
        fields.setProject(new Project());
        fields.getProject().setKey(getProjectName());

        // Priority
        fields.setPriority(new Priority());
        fields.getPriority().setName(getPriorityName());

        // Assignee
        Assignee assignee = new Assignee(getAssigneeName());
        fields.setAssignee(assignee);

        // Data Element
        String dataElement = getDataElement();
        List<String> dataElementList = new ArrayList<>();
        dataElementList.add(dataElement);
        fields.setDataElement(dataElementList);

        // Country
        Country country = new Country();
        country.setValue(getCountry());
        List<Country> countryList = new ArrayList<>();
        countryList.add(country);
        fields.setCountry(countryList);

        // Business Line
        BusinessLine businessLine = new BusinessLine();
        businessLine.setValue(getBusinessLineName());
        List<BusinessLine> businessLineList = new ArrayList<BusinessLine>();
        businessLineList.add(businessLine);
        fields.setBusinessLine(businessLineList);

        // Reporting Department
        ReportingDepartment reportingDepartment = new ReportingDepartment();
        reportingDepartment.setValue(getReportingDepartmentName());
        fields.setReportingDepartment(reportingDepartment);

        // Data Owner
        DataOwner dataowner = new DataOwner();
        dataowner.setValue(getDataOwner());
        List<DataOwner> dataOwnerList = new ArrayList<>();
        dataOwnerList.add(dataowner);
        fields.setDataOwner(dataOwnerList);

//        // Linked Issue
//        LinkedIssue linkedIssue = new LinkedIssue(getLinkedIssue());
//        List<LinkedIssue> linkedIssueList = new ArrayList<>();
//        linkedIssueList.add(linkedIssue);
//        fields.setIssuelinks(linkedIssueList);


        getLogging().logDebug(procName, "issue type is >" + getIssueTypeName() +"<.");
//        issue.getFields().setIssuetype(new nl.jacbeekers.jira.IssueType(getIssueType().getId(), getIssueType().getName()));

        Gson gson = new Gson();
        setBody(gson.toJson(issue));

        getLogging().logDebug(procName, "Body is >" + getBody() + "<.");

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
        this.ReportingDepartmentName = reportingDepartmentName;
    }

    public void setCreatedIssueResponse(CreatedIssueResponse createdIssueResponse) {
        this.createdIssueResponse = createdIssueResponse;
    }

    public CreatedIssueResponse getCreatedIssueResponse() {
        return createdIssueResponse;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getBusinessLineName() {
        return businessLineName;
    }
    public void setBusinessLineName(String businessLineName) {
        this.businessLineName = businessLineName;
    }

    public String getAssigneeName() {
        return assigneeName;
    }
    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public Assignee getAssignee() {
        return assignee;
    }
    public void setAssignee(Assignee assignee) {
        this.assignee = assignee;
    }

    public String getDataElement() {
        return dataElement;
    }
    public void setDataElement(String dataElement) {
        this.dataElement = dataElement;
    }

    public String getImpactDescription() {return impactDescription; }
    public void setImpactDescription(String impactDescription) {
        this.impactDescription = impactDescription;
    }

    public String getAcceptanceCriteria() {
        return acceptanceCriteria;
    }
    public void setAcceptanceCriteria(String acceptanceCriteria) {
        this.acceptanceCriteria = acceptanceCriteria;
    }

    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }

    public String getDataOwner() {return dataOwner;}
    public void setDataOwner(String dataOwner) {this.dataOwner = dataOwner;}

    public Date getDueDate() {
        return dueDate;
    }
    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getPriorityName() {
        return priorityName;
    }
    public void setPriorityName(String priorityName) {
        this.priorityName = priorityName;
    }

    public Region getRegion() {
        return this.region;
    }
    public void setRegion(Region region) {
        this.region = region;
    }

    public String getRegionName() {
        return regionName;
    }
    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getLinkedIssue() {return linkedIssue; }
    public void setLinkedIssue(String linkedIssue) {this.linkedIssue = linkedIssue; }

    public String getBody() {return body; }
    public void setBody(String body) {this.body = body; }
}
