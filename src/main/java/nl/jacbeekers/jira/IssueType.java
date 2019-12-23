package nl.jacbeekers.jira;

public class IssueType {
    String id = null;
    String name = null;

    public IssueType() {

    }
    public IssueType(String id, String name) {
        setId(id);
        setName(name);
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return this.name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
