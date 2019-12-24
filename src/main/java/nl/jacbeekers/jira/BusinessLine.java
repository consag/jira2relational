package nl.jacbeekers.jira;

public class BusinessLine {
    /***
     * ,"customfield_21200":{
     * 	"self":"https:\/\/jira.bb8-ta.aws.abnamro.org\/rest\/api\/2\/customFieldOption\/33101"
     * 	,"value":"Corporate & Institutional Banking"
     * 	,"id":"33101"
     * 	,"child":{
     * 		"self":"https:\/\/jira.bb8-ta.aws.abnamro.org\/rest\/api\/2\/customFieldOption\/33118"
     * 		,"value":"Americas"
     * 		,"id":"33118"
     *        }
     * }
     */
    String self;
    String value;
    String id;
    Region region;

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }
}
