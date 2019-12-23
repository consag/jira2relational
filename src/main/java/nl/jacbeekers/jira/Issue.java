package nl.jacbeekers.jira;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Issue {
/***
 *          {   "fields": {
 *             "project":
 *             {
 *                 "key": "TEST"
 *             },
 *             "summary": "REST ye merry gentlemen.",
 *                     "description": "Creating of an issue using project keys and issue type names using the REST API",
 *                     "issuetype": {
 *                 "name": "Bug"
 *             }
 *         }
 *         }
 */
    private Fields fields;
    // getters and setters

    public Fields getFields() {
        return this.fields;
    }

    public void setFields(Fields fields) {
        this.fields = fields;
    }
}