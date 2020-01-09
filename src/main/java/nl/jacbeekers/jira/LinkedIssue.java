package nl.jacbeekers.jira;

import java.security.PrivateKey;

public class LinkedIssue {

    String id;
    String self;
    LinkType linkType;
    OutwardIssue outwardIssue;
    String linkedIssue;

    public LinkedIssue(String linkedIssue) {
        this.linkType = new LinkType();
        this.outwardIssue = new OutwardIssue(linkedIssue);
    }

    private class LinkType {

        String id;
        String name;
        String inward;
        String outward;
        String self;

        public LinkType() {
            this.name = "Relates";
            this.inward = "relates to";
            this.outward = "relates to";
        }
    }

    private class OutwardIssue {
        String id;
        String key;
        String self;

        public OutwardIssue(String key) {
            this.key = key;
        }
    }
}




