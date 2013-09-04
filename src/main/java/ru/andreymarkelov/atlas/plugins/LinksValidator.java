/*
 * Created by Andrey Markelov 02-10-2012.
 * Copyright Mail.Ru Group 2012. All rights reserved.
 */
package ru.andreymarkelov.atlas.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.Validator;
import com.opensymphony.workflow.WorkflowException;

public class LinksValidator
    implements Validator
{
    /**
     * Link manager.
     */
    private final IssueLinkManager ilMgr;

    /**
     * Link type manager.
     */
    private final IssueLinkTypeManager iltMgr;

    /**
     * Constrcutor.
     */
    public LinksValidator(
        IssueLinkManager ilMgr,
        IssueLinkTypeManager iltMgr)
    {
        this.ilMgr = ilMgr;
        this.iltMgr = iltMgr;
    }

    @Override
    public void validate(
        Map transientVars,
        Map args,
        PropertySet ps)
    throws InvalidInputException, WorkflowException
    {
        Issue issue = (Issue) transientVars.get("issue");

        List<String> lsts = new ArrayList<String>();
        String statuses = (String) args.get("status");
        StringTokenizer st = new StringTokenizer(statuses, "&");
        while (st.hasMoreTokens())
        {
            lsts.add(st.nextToken());
        }

        String projkey = (String) args.get("projkey");
        String issuetype = (String) args.get("issuetype");
        String linktype = (String) args.get("linktype");
        IssueLinkType itl = iltMgr.getIssueLinkType(Long.parseLong(linktype));

        Collection<IssueLink> lc = ilMgr.getInwardLinks(issue.getId());
        for (IssueLink il : lc)
        {
            if (il.getIssueLinkType().equals(itl))
            {
                Issue destIssue = il.getSourceObject();
                if (destIssue != null &&
                    destIssue.getProjectObject().getKey().equals(projkey) &&
                    destIssue.getIssueTypeObject().getId().equals(issuetype) &&
                    lsts.contains(destIssue.getStatusObject().getId()))
                {
                    throw new InvalidInputException(
                        ComponentManager.getInstance().getJiraAuthenticationContext().getI18nHelper().getText("utils.linkerror"));
                }
            }
        }
    }
}
