package nl.jacbeekers.jira;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
//import sun.awt.image.ImageWatched;

public class IssueLink {

    @SerializedName("type")
    @Expose
    public LinkType type;
    @SerializedName("inwardIssue")
    @Expose
    public InwardIssue inwardIssue;
    @SerializedName("outwardIssue")
    @Expose
    public OutwardIssue outwardIssue=null;

    public void setType(LinkType type){this.type=type;}
    public LinkType getType(){
        return this.type;
    }

    public void setInwardIssue(InwardIssue inwardIssue){this.inwardIssue=inwardIssue;}
    public InwardIssue getInwardIssue(){return inwardIssue;}

    public void setOutwardIssue(OutwardIssue outwardIssue){this.outwardIssue=outwardIssue;}
    public OutwardIssue getOutwardIssue(){return outwardIssue;}

}
