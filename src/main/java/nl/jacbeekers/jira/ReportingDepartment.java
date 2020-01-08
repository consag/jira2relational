package nl.jacbeekers.jira;

public class ReportingDepartment {
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

    public class Child {
        String self;
        String value;
        String id;

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

    String self;
    String value;
    String id;
    Child child;

    public String getSelf() { return self; }

    public void setSelf(String self) { this.self = self; }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
