package nl.jacbeekers.jira;

public class Region {
    /***
     *      * 	,"child":{
     *      * 		"self":"https:\/\/jira.bb8-ta.aws.abnamro.org\/rest\/api\/2\/customFieldOption\/33118"
     *      * 		,"value":"Americas"
     *      * 		,"id":"33118"
     *      *        }
     */
    private String self;
    private String value;
    private String id;

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
}
