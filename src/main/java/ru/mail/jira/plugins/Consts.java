/*
 * Created by Andrey Markelov 02-10-2012. Copyright Mail.Ru Group 2012. All
 * rights reserved.
 */
package ru.mail.jira.plugins;


public interface Consts
{
    String ALL_GROUPS = "all-groups";

    String ALL_STATUSES = "statuses";

    String CUSTOM_FIELD_ID = "cfId";

    String JQL = "jqlstr";

    String MSG = "msg";

    String REGEX = "regex";

    String SELECTED_GROUPS = "selectedGroupsList";

    String SELECTED_GROUPS_SET = "selectedGroupsListSet";

    String SELECTED_STATUS = "selectedStatus";

    String SPLITTER = "stlitter";

    String ISSUE_PROJECT = "issueProject";

    String ISSUE_TYPE = "issueType";

    String ISSUE_STATUS = "issueStatus";

    String CLONE_PREFIX = "clonePrefix";

    String CLONE_ASSIGNEE = "cloneAssignee";

    String CLONE_LINK_TYPE = "cloneLink";

    /**
     * Issue clone count.
     */
    String ISSUE_CLONE_COUNT = "issueCloneCount";

    /**
     * Is clone with attachments?
     */
    String ISSUE_CLONE_ATTACHMENTS = "isCloneWithAttchments";

    /**
     * Is clone with links?
     */
    String ISSUE_CLONE_LINKS = "isCloneWithLinks";
}
