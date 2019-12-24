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

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import nl.jacbeekers.jira.IssueResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

public class JiraCall {
    private Logging logging = new Logging(Logger.getLogger(JiraCall.class.getName()));

    // httpClient
    CloseableHttpClient httpClient;
    BasicCookieStore basicCookieStore;

    // response
    private String response = Constants.UNKNOWN;

    // login
    private String username = Constants.NOT_PROVIDED;
    private String password = Constants.NOT_PROVIDED;
    private String loginURL = Constants.NOT_PROVIDED;
    private String queryURL = Constants.NOT_PROVIDED;
    private IssueResponse issueResponse;

    // Create an issue
    private String projectName = Constants.NOT_PROVIDED;
    private String issueTypeId = Constants.NOT_PROVIDED;
    private String issueTypeName = Constants.NOT_PROVIDED;

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

        return getLogging().getResultCode();
    }


    /***
     * Build the httpClient
     */
    public CloseableHttpClient createHttpClient() {
        BasicCookieStore basicCookieStore = new BasicCookieStore();
        setBasicCookieStore(basicCookieStore);
        TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
        SSLContext sslContext=null;
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        } catch(Exception e) {
            getLogging().logError(Constants.LOGIN_FAILED, "SSL exception occurred: " + e.toString());
        }

        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext,
                NoopHostnameVerifier.INSTANCE);
        Registry<ConnectionSocketFactory> socketFactoryRegistry =
                RegistryBuilder.<ConnectionSocketFactory> create()
                        .register("https", sslSocketFactory)
                        .register("http", new PlainConnectionSocketFactory())
                        .build();

        Header header = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        List<Header> defaultHeaders = Lists.newArrayList(header);

        CloseableHttpClient httpClient = HttpClientBuilder
                .create()
                .setDefaultCookieStore(basicCookieStore)
                 .setSSLSocketFactory(sslSocketFactory)
                 .setDefaultHeaders(defaultHeaders)
                .build();
        setHttpClient(httpClient);
        return httpClient;
    }

    public void createHttpClient(String proxyHostname, int proxyPortnumber) {
        BasicCookieStore basicCookieStore = new BasicCookieStore();
        setBasicCookieStore(basicCookieStore);
        CloseableHttpClient httpClient = null;

        TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
        SSLContext sslContext=null;
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        } catch(Exception e) {
            getLogging().logError(Constants.LOGIN_FAILED, "SSL exception occurred: " + e.toString());
        }

        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext,
                NoopHostnameVerifier.INSTANCE);
        Registry<ConnectionSocketFactory> socketFactoryRegistry =
                RegistryBuilder.<ConnectionSocketFactory> create()
                        .register("https", sslSocketFactory)
                        .register("http", new PlainConnectionSocketFactory())
                        .build();

        if(proxyHostname == null) {
            httpClient = createHttpClient();
        } else {
            getLogging().logDebug("Using proxy >" + proxyHostname +"< with port >" + proxyPortnumber +"<.");
            HttpHost proxy = new HttpHost(proxyHostname, proxyPortnumber);
            httpClient = HttpClientBuilder
                    .create()
                    .setProxy(proxy)
                    .setDefaultCookieStore(basicCookieStore)
                    .setSSLSocketFactory(sslSocketFactory)
                    .build();
        }

        setHttpClient(httpClient);
    }

    private void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CloseableHttpClient getHttpClient() {
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
        CloseableHttpResponse httpResponse = null;
        int statusCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;

        getLogging().setResultCode(Constants.OK);
        getLogging().setResultMessage("No errors encountered.");

        HttpGet httpGet = new HttpGet(getLoginURL());
        String auth = getUsername() + ":" + getPassword();
        getLogging().logDebug("username is >" + getUsername() +"<.");
//        logDebug("password is >" + getPassword() +"<.");

        byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(StandardCharsets.ISO_8859_1));
        String authHeader = "Basic " + new String(encodedAuth);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, authHeader);

        try {
            httpResponse = getHttpClient().execute(httpGet);
            statusCode = httpResponse.getStatusLine()
                    .getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                getLogging().logError(Constants.LOGIN_FAILED, "Login failed with HTTP code >" + statusCode + "<.");
            } else {
                getLogging().logDebug("Login successful.");
                processHttpResponse( httpResponse);
                // list cookies
                List<Cookie> cookieList = basicCookieStore.getCookies();
                ListIterator<Cookie> cookieListIterator = cookieList.listIterator();
                while (cookieListIterator.hasNext()) {
                    Cookie cookie = cookieListIterator.next();
                    getLogging().logDebug(procName, "Cookie found >" + cookie.getName() + "< with value >" + cookie.getValue() + "<.");
                }

            }

        } catch (IOException e) {
            getLogging().logError(Constants.LOGIN_FAILED, "Exception occurred during login request. Exception: " + e.toString());
        } finally {
            try {
                httpResponse.close();
            } catch (IOException e) {
                getLogging().logWarning( "Could not close response object for login. Exception: " + e.toString());
            }
        }

    }

    public boolean projectExists() {
        CloseableHttpResponse httpResponse = null;
        // GET /rest/api/latest/project/<projectKey>
        String completeQueryURL = getQueryURL() +"/project/" + getProjectName();
        boolean rc = false;
        httpResponse = doGet(completeQueryURL);
        int code = processHttpResponse(httpResponse);
        switch (code) {
            case HttpStatus.SC_OK:
                rc= true;
                break;
            case HttpStatus.SC_NOT_FOUND:
                rc= false;
                break;
            default:
                getLogging().logError(Constants.PROJECT_CHECK_FAILED, "Project existence check returned HTTP error >" + code + "<.");
                break;
        }

        try {
            httpResponse.close();
        } catch(IOException e) {
            getLogging().logWarning("Could not close response. Exception: " + e.toString());
        }

        return rc;
    }

    public boolean projectExists(String projectName) {
        if(projectName != null) {
            setProjectName(projectName);
        }
        return projectExists();
    }

    public String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        }catch (UnsupportedEncodingException e) {
            getLogging().logError(Constants.ISSUETYPE_RETRIEVAL_FAILED, "Exception occurred encoding string >" + value
                    +"<. Exception: " + e.toString());
            return Constants.ISSUETYPE_RETRIEVAL_FAILED;
        }
    }
    public boolean issueTypeExists() {
        boolean rc = false;
        String procName="issueTypeExists";
        CloseableHttpResponse httpResponse = null;
        // GET /rest/api/latest/project/<projectKey>
        // Jira before 8.4:
        // /rest/api/2/issue/createmeta?projectKeys=JRA&issuetypeNames=Bug&expand=projects.issuetypes.fields
//        String completeQueryURL = getQueryURL() + "/createmeta?projectKeys=" + getProjectName()
//                +"&issuetypeNames=" + encodeValue(getIssueTypeName()) + "&expand=projects.issuetypes.fields";
        String completeQueryURL = getQueryURL() + "/createmeta";
        getLogging().logDebug(procName, "URL is >" + completeQueryURL +"<.");
        httpResponse = doGet(completeQueryURL);
        int code = processHttpResponse(httpResponse);
        switch (code) {
            case HttpStatus.SC_OK:
                rc= true;
                break;
            case HttpStatus.SC_NOT_FOUND:
                rc= false;
                break;
            default:
                getLogging().logError(Constants.PROJECT_CHECK_FAILED, "Issue Type existence check returned HTTP error >" + code + "<.");
                break;
        }
        try {
            httpResponse.close();
        } catch(IOException e) {
            getLogging().logWarning("Could not close response. Exception: " + e.toString());
        }

        return rc;
    }

    public boolean issueTypeExists(String issueTypeId) {
        setIssueTypeId(issueTypeId);
        return  issueTypeExists();
    }

    /***
     *
     */
    public IssueResponse queryJiraForIssue(String jiraId, ArrayList<String> fields) {
        String procName = "queryJiraForIssue";
        CloseableHttpResponse httpResponse = null;
//        ResponseHandler<String> responseHandler = new BasicResponseHandler();

        getLogging().logDebug(procName + " - Trying to get issue >" + jiraId + "<.");
        String completeQueryURL = getQueryURL() +"/issue/" + jiraId;
        if (fields == null) {
            getLogging().logDebug("No fields specified. All fields will be retrieved.");
        } else {
            getLogging().logDebug("Field list provided.");
            //Need to add fields as comma-separated list
            String csv = String.join(",", fields);
            getLogging().logDebug("Comma separated field list is >" + csv +"<.");
            completeQueryURL +="?fields=" + csv;
            getLogging().logDebug("Complete URL is >" + completeQueryURL +"<.");
        }
        httpResponse = doGet(completeQueryURL);

        return processHttpResponse(jiraId, httpResponse);
    }

    /***
     *
     * @param theURL to GET
     * @return HttpResponse
     */
    private CloseableHttpResponse doGet(String theURL) {
        CloseableHttpResponse httpResponse = null;
        HttpGet httpGet = new HttpGet(theURL);

        try {
            httpResponse = getHttpClient().execute(httpGet);
        } catch (IOException e) {
            getLogging().logError(Constants.QUERY_FAILED, "Exception occurred during query request. Exception: " + e.toString());
        }
        return httpResponse;
    }

    private int processHttpResponse(CloseableHttpResponse httpResponse) {
        int statusCode = httpResponse.getStatusLine()
                .getStatusCode();

        switch (statusCode) {
            case HttpStatus.SC_OK:
                getLogging().logDebug("HTTP request returned an OK.");
                break;
            case HttpStatus.SC_NOT_FOUND:
                getLogging().logDebug("HTTP request returned a NOT FOUND.");
                break;
            default:
                getLogging().logError(Constants.QUERY_FAILED, "An HTTP error was returned: " + statusCode);
        }

        return statusCode;
    }

    private IssueResponse processHttpResponse(String jiraId, CloseableHttpResponse httpResponse) {
        processHttpResponse(httpResponse);

        IssueResponse issueResponse = null;
//        jiraIssue.setKey(jiraId);

        HttpEntity httpEntity = httpResponse.getEntity();

        if (httpEntity == null) {
            getLogging().logError(Constants.QUERY_FAILED, "Could not get response content for jira issue >" + jiraId + "<.");
        } else {
            try {
                String resultString = EntityUtils.toString(httpEntity);
                JSONObject jsonObject = new JSONObject(resultString);
                getLogging().logDebug("Id is >"+ jsonObject.getString("id") + "<.");
                getLogging().logDebug("Key is >"+ jsonObject.getString("key") + "<.");
                getLogging().logDebug("Fields content is >"+ jsonObject.getString("fields") + "<.");
                Gson gson = new Gson();
                getLogging().logDebug("Converting response into Java object...");
                issueResponse = gson.fromJson(resultString, IssueResponse.class);
                setIssueResponse(issueResponse);
                getLogging().logDebug("Key in Java object is >" + issueResponse.key +"<.");
                getLogging().logDebug("Status id is >" + getStatusId() +"<.");
                getLogging().logDebug("Status name is >" + getStatusName() +"<.");
//                logging.logDebug("Status category id is >" + issueResponse.fields.status.statusCategory.id +"<.");
//                logging.logDebug("Status category name is >" + issueResponse.fields.status.statusCategory.name +"<.");
//                logging.logDebug("Status category key is >" + issueResponse.fields.status.statusCategory.key +"<.");

            } catch (IOException e) {
                getLogging().logError(Constants.QUERY_FAILED, "Could not parse JSON response. Exception: " + e.toString());
            } catch (JSONException j) {
                getLogging().logError(Constants.QUERY_FAILED, "JSON Exception: " + j.toString());
            }
        }

        return issueResponse;
    }

    public void close() {
        try {
            getHttpClient().close();
            getLogging().logDebug("HTTP Connection closed.");
        } catch(IOException e) {
            getLogging().logWarning("Could not close HTTP Connection. Exception: " + e.toString());
        }
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

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setIssueTypeId(String issueTypeId) {
        this.issueTypeId = issueTypeId;
    }
    public String getIssueTypeId() {
        return issueTypeId;
    }

    public void setIssueTypeName(String issueTypeName) {
        this.issueTypeName = issueTypeName;
    }
    public String getIssueTypeName() {
        return issueTypeName;
    }
    public Logging getLogging() { return this.logging; }

    //convenience getters
    public String getStatusName() {
        return getIssueResponse().fields.status.name;
    }
    public String getStatusId() {
        return getIssueResponse().fields.status.id;
    }

}


