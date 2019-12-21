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

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.google.gson.Gson;
import nl.jacbeekers.jira.IssueResponse;
import nl.jacbeekers.jira.Status;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class JiraCall {
    private static final org.apache.log4j.Logger logger = Logger.getLogger(JiraCall.class.getName());

    private String resultCode = Constants.OK;
    private String resultMessage = Constants.getResultMessage(resultCode);

    // httpClient
    HttpClient httpClient;
    BasicCookieStore basicCookieStore;

    // response
    private String response = Constants.UNKNOWN;

    // login
    private String username = Constants.NOT_PROVIDED;
    private String password = Constants.NOT_PROVIDED;
    private String loginURL = Constants.NOT_PROVIDED;
    private String queryURL = Constants.NOT_PROVIDED;
    private IssueResponse issueResponse;

    /***
     *
     * @param username
     * @param password
     * @return
     * @throws IOException
     */
    public String login(String username, String password) throws IOException {
        setUsername(username);
        setPassword(password);
        loginRequest();

        return getResultCode();
    }


    /***
     * Build the httpClient
     */
    public HttpClient createHttpClient() {
        BasicCookieStore basicCookieStore = new BasicCookieStore();
        setBasicCookieStore(basicCookieStore);
         HttpClient httpClient = HttpClientBuilder
                .create()
                .setDefaultCookieStore(basicCookieStore)
                .build();
        setHttpClient(httpClient);
        return httpClient;
    }

    public void createHttpClient(String proxyHostname, int proxyPortnumber) {
        BasicCookieStore basicCookieStore = new BasicCookieStore();
        setBasicCookieStore(basicCookieStore);
        HttpClient httpClient = null;

        if(proxyHostname == null) {
            httpClient = createHttpClient();
        } else {
            logDebug("Using proxy >" + proxyHostname +"< with port >" + proxyPortnumber +"<.");
            HttpHost proxy = new HttpHost(proxyHostname, proxyPortnumber);
            httpClient = HttpClientBuilder
                    .create()
                    .setProxy(proxy)
                    .setDefaultCookieStore(basicCookieStore)
                    .build();
        }

        setHttpClient(httpClient);
    }

    private void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    private void setBasicCookieStore(BasicCookieStore basicCookieStore) {
        this.basicCookieStore = basicCookieStore;
    }

    /***
     *
     */
    private void loginRequest() {
        String procName = "loginRequest";
        HttpResponse httpResponse = null;
        int statusCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;

        HttpGet httpGet = new HttpGet(getLoginURL());
        String auth = getUsername() + ":" + getPassword();
        byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(StandardCharsets.ISO_8859_1));
        String authHeader = "Basic " + new String(encodedAuth);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, authHeader);

        try {
            httpResponse = getHttpClient().execute(httpGet);
            statusCode = httpResponse.getStatusLine()
                    .getStatusCode();
        } catch (IOException e) {
            logError(Constants.LOGIN_FAILED, "Exception occurred during login request. Exception: " + e.toString());
        }

        if (statusCode != HttpStatus.SC_OK) {
            logError(Constants.LOGIN_FAILED, "Login failed with HTTP code >" + statusCode + "<.");
        } else {
            logDebug("Login successful.");
            processHttpResponse( httpResponse);
            // list cookies
            List<Cookie> cookieList = basicCookieStore.getCookies();
            ListIterator<Cookie> cookieListIterator = cookieList.listIterator();
            while (cookieListIterator.hasNext()) {
                Cookie cookie = cookieListIterator.next();
                logDebug(procName, "Cookie found >" + cookie.getName() + "< with value >" + cookie.getValue() + "<.");
            }

        }
    }


    /***
     *
     */
    public IssueResponse queryJiraForIssue(String jiraId, ArrayList<String> fields) {
        String procName = "queryJiraForIssue";
        HttpResponse httpResponse = null;
        ResponseHandler<String> responseHandler = new BasicResponseHandler();

        logDebug(procName + " - Trying to get issue >" + jiraId + "<.");
        String completeQueryURL = getQueryURL() +"/" + jiraId;
        if (fields == null) {
            logDebug("No fields specified. All fields will be retrieved.");
        } else {
            logDebug("Field list provided.");
            //Need to add fields as comma-separated list
            String csv = String.join(",", fields);
            logDebug("Comma separated field list is >" + csv +"<.");
            completeQueryURL +="?fields=" + csv;
            logDebug("Complete URL is >" + completeQueryURL +"<.");
        }

        HttpGet httpGet = new HttpGet(completeQueryURL);

        try {
            httpResponse = getHttpClient().execute(httpGet);
        } catch (IOException e) {
            logError(Constants.QUERY_FAILED, "Exception occurred during query request. Exception: " + e.toString());
        }

        return processHttpResponse(jiraId, httpResponse);
    }

    private void processHttpResponse(HttpResponse httpResponse) {
        int statusCode = httpResponse.getStatusLine()
                .getStatusCode();

        switch (statusCode) {
            case HttpStatus.SC_OK:
                logDebug("HTTP request returned an OK.");
                break;
            case HttpStatus.SC_NOT_FOUND:
                logDebug("HTTP request returned a NOT FOUND.");
                break;
            default:
                logError(Constants.QUERY_FAILED, "An HTTP error was returned: " + statusCode);
        }

    }

    private IssueResponse processHttpResponse(String jiraId, HttpResponse httpResponse) {
        processHttpResponse(httpResponse);

        IssueResponse issueResponse = null;
//        jiraIssue.setKey(jiraId);

        HttpEntity httpEntity = httpResponse.getEntity();

        if (httpEntity == null) {
            logError(Constants.QUERY_FAILED, "Could not get response content for jira issue >" + jiraId + "<.");
        } else {
            try {
                String resultString = EntityUtils.toString(httpEntity);
                JSONObject jsonObject = new JSONObject(resultString);
                logDebug("Id is >"+ jsonObject.getString("id") + "<.");
                logDebug("Key is >"+ jsonObject.getString("key") + "<.");
                logDebug("Fields content is >"+ jsonObject.getString("fields") + "<.");
                Gson gson = new Gson();
                logDebug("Converting response into Java object...");
                issueResponse = gson.fromJson(resultString, IssueResponse.class);
                setIssueResponse(issueResponse);
                logDebug("Key in Java object is >" + issueResponse.key +"<.");
                logDebug("Status id is >" + getStatusId() +"<.");
                logDebug("Status name is >" + getStatusName() +"<.");
//                logDebug("Status category id is >" + issueResponse.fields.status.statusCategory.id +"<.");
//                logDebug("Status category name is >" + issueResponse.fields.status.statusCategory.name +"<.");
//                logDebug("Status category key is >" + issueResponse.fields.status.statusCategory.key +"<.");

            } catch (IOException e) {
                logError(Constants.QUERY_FAILED, "Could not parse JSON response. Exception: " + e.toString());
            } catch (JSONException j) {
                logError(Constants.QUERY_FAILED, "JSON Exception: " + j.toString());
            }
        }

        return issueResponse;
    }


    /*
        getters setters
    */
    private void setIssueResponse(IssueResponse issueResponse) {
        this.issueResponse = issueResponse;
    }
    private IssueResponse getIssueResponse() {
        return this.issueResponse;
    }
    private String getUsername() {
        return this.username;
    }


    public void setQueryURL(String queryURL) {
        this.queryURL = queryURL;
    }

    public String getQueryURL() {
        return this.queryURL;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLoginURL() {
        return this.loginURL;
    }

    public void setLoginURL(String URL) {
        this.loginURL = URL;
    }

    public String getFormattedCurrentTime() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedTime = sdf.format(date);

        return formattedTime;
    }

    public String getResultCode() {
        return this.resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMessage() {
        return this.resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    //
    // logging, result handling
    //
    private void logVerbose(String msg) {
        logger.trace(msg);
    }

    private void logDebug(String procName, String msg) {
        logger.debug(procName + " - " + msg);
    }

    private void logDebug(String msg) {
        logger.debug(msg);
    }

    private void logWarning(String msg) {
        logger.warn(msg);
    }

    private void logError(String resultCode, String msg) {
        setResult(resultCode, msg);
        logger.error(msg);
    }

    private void setResult(String resultCode, String msg) {
        setResultCode(resultCode);
        if (msg == null) {
            setResultMessage(Constants.getResultMessage(resultCode));
        } else {
            setResultMessage(Constants.getResultMessage(resultCode)
                    + ": " + msg);
        }
    }

    private void logFatal(String resultCode) {
        logFatal(resultCode, Constants.getResultMessage(resultCode));
    }

    private void logFatal(String resultCode, String msg) {
        setResult(resultCode, msg);
        logger.fatal(msg);
    }

    private void failSession(String resultCode) {
        failSession(resultCode, null);
    }

    private void failSession(String resultCode, String msg) {
        logError(resultCode, msg);
    }


    //convenience getters
    public String getStatusName() {
        return getIssueResponse().fields.status.name;
    }
    public String getStatusId() {
        return getIssueResponse().fields.status.id;
    }

}


