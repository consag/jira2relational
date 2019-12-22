# Introduction 
Jira API Client 

## Goals
- Read the status of a Jira issue, specified by its key
- Create a new Jira issue

## Reason
- Informatica tooling is not capable of using proxies, SSL and JSON structures in a flexible way.
- Java code can be added to Informatica, so only the necessary input needs to given to the Java class JiraCall
- Java code takes care of proxies and SSL certificates

## Current limitations
- Basic Authentication has been implemented

## Future Plans
- Create a new Jira issue
- Update a Jira issue
- Support JQL query to search for Jira issues

