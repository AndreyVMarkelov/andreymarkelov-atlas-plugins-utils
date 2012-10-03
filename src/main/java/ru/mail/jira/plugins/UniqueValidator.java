/*
 * Created by Andrey Markelov 02-10-2012.
 * Copyright Mail.Ru Group 2012. All rights reserved.
 */
package ru.mail.jira.plugins;

import java.util.Map;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.web.bean.PagerFilter;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.Validator;
import com.opensymphony.workflow.WorkflowException;

public class UniqueValidator
    implements Validator
{
    private final CustomFieldManager customFieldManager;

    private final SearchService searchService;

    /**
     * Constructor.
     */
    public UniqueValidator(
        CustomFieldManager customFieldManager,
        SearchService searchService)
    {
        this.customFieldManager = customFieldManager;
        this.searchService = searchService;
    }

    @Override
    public void validate(
        Map transientVars,
        Map args,
        PropertySet ps)
    throws InvalidInputException, WorkflowException
    {
        Issue issue = (Issue) transientVars.get("issue");

        String cfId = (String) args.get(Consts.CUSTOM_FIELD_ID);
        String jql = (String) args.get(Consts.JQL);

        if (!Utils.isValidStr(cfId) || !Utils.isValidStr(jql))
        {
            return;
        }

        try
        {
            Long.parseLong(cfId);
        }
        catch (NumberFormatException nex)
        {
            return;
        }

        CustomField customField = customFieldManager.getCustomFieldObject(Long.parseLong(cfId));
        if (customField != null)
        {
            Object cfVal = issue.getCustomFieldValue(customField);
            if (cfVal == null)
            {
                throw new InvalidInputException(String.format("The field '%s' is required", customField.getName()));
            }

            User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
            if (user == null)
            {
                return;
            }

            SearchService.ParseResult parseResult = searchService.parseQuery(user, jql);
            if (parseResult.isValid())
            {
                try
                {
                    SearchResults results = searchService.search(user, parseResult.getQuery(), PagerFilter.getUnlimitedFilter());
                    if (results != null)
                    {
                        for (Issue i : results.getIssues())
                        {
                            Object oldVal = i.getCustomFieldValue(customField);
                            if (oldVal != null)
                            {
                                if (cfVal.toString().equals(oldVal.toString()))
                                {
                                    throw new InvalidInputException(String.format("Values of field '%s' must be unique", customField.getName()));
                                }
                            }
                        }
                    }
                }
                catch (SearchException e)
                {
                    return;
                }
            }
        }
    }
}
