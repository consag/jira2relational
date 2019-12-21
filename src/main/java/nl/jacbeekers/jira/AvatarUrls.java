package nl.jacbeekers.jira;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AvatarUrls {

    @SerializedName("48x48")
    @Expose
    public String _48x48;
    @SerializedName("24x24")
    @Expose
    public String _24x24;
    @SerializedName("16x16")
    @Expose
    public String _16x16;
    @SerializedName("32x32")
    @Expose
    public String _32x32;

}
