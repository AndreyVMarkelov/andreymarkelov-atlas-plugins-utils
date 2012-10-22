/*
 * Created by Andrey Markelov 02-10-2012.
 * Copyright Mail.Ru Group 2012. All rights reserved.
 */
package ru.mail.jira.plugins;

import java.util.Map;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.WorkflowException;

/**
 * Assign to custom field value post function.
 * 
 * @author Andrey Markelov
 *
 */
public class AssignPostFunction
    extends AbstractJiraFunctionProvider
{
    /**
     * Custom field manager.
     */
    private final CustomFieldManager cfMgr;

    /**
     * User manager.
     */
    private final UserManager userManager;

    /**
     * Constructor.
     */
    public AssignPostFunction(
        CustomFieldManager cfMgr,
        UserManager userManager)
    {
        this.cfMgr = cfMgr;
        this.userManager = userManager;
    }

    @Override
    public void execute(
        Map transientVars,
        Map args,
        PropertySet ps)
    throws WorkflowException
    {
        MutableIssue issue = getIssue(transientVars);

        String cfId = (String) args.get(Consts.CUSTOM_FIELD_ID);

        if (!Utils.isValidStr(cfId))
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

        CustomField customField = cfMgr.getCustomFieldObject(Long.parseLong(cfId));
        if (customField != null)
        {
        	Object cfVal = issue.getCustomFieldValue(customField);
            if (cfVal == null)
            {
                throw new InvalidInputException(String.format("The field '%s' is required", customField.getName()));
            }

            String userName;
            if (cfVal.toString().contains(":"))
            {
                userName = cfVal.toString().substring(0, cfVal.toString().indexOf(":"));
            }
            else
            {
                userName = cfVal.toString();
            }

            User user = userManager.getUser(userName);
            if (user != null)
            {
                issue.setAssignee(user);
            }
            else
            {
                throw new InvalidInputException(String.format("The field '%s' must contain user", customField.getName()));
            }
        }
    }
}
