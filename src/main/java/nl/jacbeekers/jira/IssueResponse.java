package nl.jacbeekers.jira;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class IssueResponse {

    @SerializedName("expand")
    @Expose
    public String expand;
    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("self")
    @Expose
    public String self;
    @SerializedName("key")
    @Expose
    public String key;
    @SerializedName("fields")
    @Expose
    public Fields fields;

}