package nl.jacbeekers.jira;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import nl.jacbeekers.jira.StatusCategory;

public class Status {

    @SerializedName("self")
    @Expose
    public String self;
    @SerializedName("description")
    @Expose
    public String description;
    @SerializedName("iconUrl")
    @Expose
    public String iconUrl;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("statusCategory")
    @Expose
    public StatusCategory statusCategory;

}
