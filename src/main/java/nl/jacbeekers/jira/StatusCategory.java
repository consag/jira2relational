package nl.jacbeekers.jira;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StatusCategory {

    @SerializedName("self")
    @Expose
    public String self;
    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("key")
    @Expose
    public String key;
    @SerializedName("colorName")
    @Expose
    public String colorName;
    @SerializedName("name")
    @Expose
    public String name;

}
