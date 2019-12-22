package nl.jacbeekers.jira;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Progress {

    @SerializedName("progress")
    @Expose
    public Integer progress;
    @SerializedName("total")
    @Expose
    public Integer total;

}
