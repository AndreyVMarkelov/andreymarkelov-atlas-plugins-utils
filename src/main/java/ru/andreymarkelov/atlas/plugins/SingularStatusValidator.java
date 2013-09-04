package ru.andreymarkelov.atlas.plugins;


import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.util.I18nHelper;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.Validator;
import com.opensymphony.workflow.WorkflowException;


public class SingularStatusValidator implements Validator
{
    private final StatusManager statusManager;

    private final Logger log = Logger.getLogger(SingularStatusValidator.class);

    /**
     * Constructor.
     */
    public SingularStatusValidator(StatusManager statusManager)
    {
        this.statusManager = statusManager;
    }

    @Override
    public void validate(Map transientVars, Map args, PropertySet ps)
        throws InvalidInputException, WorkflowException
    {
        Issue issue = (Issue) transientVars.get("issue");

        String issueTypeParam = (String) args.get(Consts.ISSUE_TYPE);
        String issueStatusParam = (String) args.get(Consts.ISSUE_STATUS);

        if (!Utils.isValidStr(issueTypeParam)
            || !Utils.isValidStr(issueStatusParam))
        {
            log.error("SingularStatusValidator:validate - Invalid params");
            return;
        }

        try
        {
            Long.valueOf(issueTypeParam);
            Long.valueOf(issueStatusParam);
        }
        catch (NumberFormatException e)
        {
            log.error("SingularStatusValidator:validate - Issue type or Issue status is not valid number");
            return;
        }

        if (issue == null)
        {
            log.error("SingularStatusValidator:validate - Issue is null");
            return;
        }

        Status issStatus = statusManager.getStatus(issueStatusParam);
        IssueType issType = issue.getIssueTypeObject();

        final String jqlQuery = String.format(
            "project = %s and issuetype = %s and status = %s", issue
                .getProjectObject().getKey(), issueTypeParam, issueStatusParam);

        List<Issue> issues = Utils.executeJQLQuery(jqlQuery);
        if (issues != null && issues.size() > 0)
        {
            I18nHelper i18n = ComponentManager.getInstance()
                .getJiraAuthenticationContext().getI18nHelper();

            throw new WorkflowException(i18n.getText(
                "utils.singularstatus.error", issStatus.getName(),
                issType.getName().toLowerCase(), issue.getProjectObject()
                    .getName(), issues.get(0).getKey(), issues.get(0)
                    .getSummary()));
        }

    }
}