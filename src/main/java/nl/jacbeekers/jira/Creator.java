package nl.jacbeekers.jira;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Creator {

    @SerializedName("self")
    @Expose
    public String self;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("key")
    @Expose
    public String key;
    @SerializedName("emailAddress")
    @Expose
    public String emailAddress;
    @SerializedName("avatarUrls")
    @Expose
    public AvatarUrls avatarUrls;
    @SerializedName("displayName")
    @Expose
    public String displayName;
    @SerializedName("active")
    @Expose
    public Boolean active;
    @SerializedName("timeZone")
    @Expose
    public String timeZone;

}
