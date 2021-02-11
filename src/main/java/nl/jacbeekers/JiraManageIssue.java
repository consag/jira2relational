package nl.jacbeekers;

import com.google.gson.Gson;
import nl.jacbeekers.jira.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private Priority priority;
    private String priorityName;
    private String businessLineName;
    private Region region;
    private String regionName;
    private String ReportingDepartmentName;
//    private Assignee assignee;
    private String assigneeName;
    private String dataElement;
    private String impactDescription;
//    private String acceptanceCriteria;
    private String country;
    private String dataOwner;
//    private Date dueDate;
    private String linkedIssue;
    private String body;
    private String linkBody;
    private String createdIssue;
    private Integer returncodeLinkedIssue;


    private CreatedIssueResponse createdIssueResponse;
    private Integer errorCode;
    private String errorText;

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
     // Link issues when an old issue already exists
                if(null!=getLinkedIssue()){
                    code=linkIssuePostRequest();
                }
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
//        fields.setAcceptanceCriteria(getAcceptanceCriteria());

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

        // Linked Issue
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
                setCreatedIssue(getCreatedIssueResponse().getKey());
                break;
            case HttpStatus.SC_BAD_REQUEST:
                // Make sure the error gets retrieved by the caller but the process continues
                getLogging().logError(Constants.CREATEISSUE_FAILED, "Create issue returned HTTP error >" + code + "<.");
                setErrorCode(code);
                setError(retrieveCreatedIssueError(httpResponse));
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

    private int linkIssuePostRequest(){
        String procName = "linkIssuePostRequest";
        CloseableHttpResponse httpResponse;
        String completeQueryURL = jiraConnectivity.getQueryURL()+"/issueLink/";

//Constructingtheissuelink
//CodetranslatesfromprovidedattributestoneededJava/Jiraattributes
        IssueLink issueLink = new IssueLink();

//Linktype
        issueLink.setType(new LinkType());
        issueLink.getType().setName("Relates");

//InwardIssue
        issueLink.setInwardIssue(new InwardIssue());
        issueLink.getInwardIssue().setKey(getCreatedIssue());

//OutwardIssue
        issueLink.setOutwardIssue(new OutwardIssue());
        issueLink.getOutwardIssue().setKey(getLinkedIssue());

        getLogging().logDebug(procName,"linkingissues>"+getCreatedIssue()+"<and>"+getLinkedIssue()+"<.");

        Gson gson = new Gson();
        setLinkBody(gson.toJson(issueLink));

        getLogging().logDebug(procName,"Bodyis>"+getLinkBody()+"<.");

        httpResponse = getJiraConnectivity().doPost(completeQueryURL,linkBody);
        if (null == httpResponse){
            getLogging().logError(Constants.LINKISSUE_FAILED,"getJiraConnectivity.doPostreturned>null<.");
            return HttpStatus.SC_NO_CONTENT;
        }

        int code = getJiraConnectivity().processHttpResponse(httpResponse);
        setReturncodeLinkedIssue(code);
        switch(code){
            case HttpStatus.SC_OK:
                break;
            case HttpStatus.SC_NOT_FOUND:
                break;
            case HttpStatus.SC_CREATED:
                getLogging().logDebug(procName,"Linkcreated.");
            break;
            default:
                getLogging().logError(Constants.LINKISSUE_FAILED,"CreatelinkreturnedHTTPerror>"+code+"<.");
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

    public String retrieveCreatedIssueError(CloseableHttpResponse httpResponse) {
        //{"errorMessage": [], "errors": "assignee": "User 'df' does not exist."}
        HttpEntity httpEntity = httpResponse.getEntity();
        String resultString = "Unknown error occurred.";
        try {
            resultString = EntityUtils.toString(httpEntity);
        } catch(IOException e) {
            getLogging().logError(Constants.CREATEISSUE_FAILED, "Issue not created and unable to interpret the error. Exception: " + e.toString());
        }
        return resultString;
    }

    // getters and setters
    public Logging getLogging() { return this.logging; }
    private void setLogging(Logging logging) {
        this.logging = logging;
    }

    public JiraConnectivity getJiraConnectivity() {return jiraConnectivity;}
    private void setJiraConnectivity(JiraConnectivity jiraConnectivity) {
        this.jiraConnectivity = jiraConnectivity;
    }

    public String getProxyHostname() { return proxyHostname; }
    public void setProxyHostname(String proxyHostname) {
        this.proxyHostname = proxyHostname;
    }

    public int getProxyPortnumber() { return proxyPortnumber; }
    public void setProxyPortnumber(int proxyPortnumber) {
        this.proxyPortnumber = proxyPortnumber;
    }

    public CloseableHttpClient getHttpClient() { return this.httpClient; }
    public void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public IssueType getIssueType() { return this.issueType; }
    public void setIssueType(IssueType issueType) {
        this.issueType = issueType;
    }

    public String getIssueTypeId() { return this.issueTypeId; }
    public void setIssueTypeId(String id) {
        this.issueTypeId = id;
    }

    public String getIssueTypeName() { return this.issueTypeName; }
    public void setIssueTypeName(String name) {
        this.issueTypeName = name;
    }

    public String getSummary() {return summary;}
    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {return description;}
    public void setDescription(String description) {
        this.description = description;
    }

    public String getReportingDepartmentName() { return ReportingDepartmentName; }
    public void setReportingDepartmentName(String reportingDepartmentName) {
        this.ReportingDepartmentName = reportingDepartmentName;
    }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getBusinessLineName() {return businessLineName;}
    public void setBusinessLineName(String businessLineName) {
        this.businessLineName = businessLineName;
    }

    public String getAssigneeName() { return assigneeName; }
    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public String getDataElement() { return dataElement; }
    public void setDataElement(String dataElement) {
        this.dataElement = dataElement;
    }

    public String getImpactDescription() {return impactDescription; }
    public void setImpactDescription(String impactDescription) {
        this.impactDescription = impactDescription;
    }

    public String getCountry() { return country; }
    public void setCountry(String country) {
        this.country = country;
    }

    public String getDataOwner() {return dataOwner;}
    public void setDataOwner(String dataOwner) {
        this.dataOwner = dataOwner;
    }

    public String getPriorityName() { return priorityName; }
    public void setPriorityName(String priorityName) {
        this.priorityName = priorityName;
    }

    public String getLinkedIssue() {return linkedIssue; }
    public void setLinkedIssue(String linkedIssue) {
        this.linkedIssue = linkedIssue;
    }

    public String getBody() {return body; }
    public void setBody(String body) {
        this.body = body;
    }

    public String getLinkBody() {return linkBody;}
    public void setLinkBody(String linkBody) {
        this.linkBody = linkBody;
    }

    public String getCreatedIssue() {return createdIssue; }
    public void setCreatedIssue(String createdIssue) {
        this.createdIssue = createdIssue;
    }

    public CreatedIssueResponse getCreatedIssueResponse() { return createdIssueResponse; }
    public void setCreatedIssueResponse(CreatedIssueResponse createdIssueResponse) {
        this.createdIssueResponse = createdIssueResponse;
    }

    public String getError() {return errorText; }
    public void setError(String errorText) {
        this.errorText = errorText;
    }

    public Integer getErrorCode() {return errorCode; }
    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public Integer getReturncodeLinkedIssue() {return returncodeLinkedIssue;}
    public void setReturncodeLinkedIssue(Integer returncodeLinkedIssue) {
        this.returncodeLinkedIssue = returncodeLinkedIssue;
    }

}
