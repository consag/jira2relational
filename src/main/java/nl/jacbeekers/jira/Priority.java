package nl.jacbeekers.jira;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Priority {

    @SerializedName("self")
    @Expose
    public String self;
    @SerializedName("iconUrl")
    @Expose
    public String iconUrl;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("id")
    @Expose
    public String id;

}
