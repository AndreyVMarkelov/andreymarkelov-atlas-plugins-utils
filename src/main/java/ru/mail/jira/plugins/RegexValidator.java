/*
 * Created by Andrey Markelov 02-10-2012.
 * Copyright Mail.Ru Group 2012. All rights reserved.
 */
package ru.mail.jira.plugins;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.Validator;
import com.opensymphony.workflow.WorkflowException;

public class RegexValidator
    implements Validator
{
    private final CustomFieldManager customFieldManager;

    /**
     * Constructor.
     */
    public RegexValidator(
        CustomFieldManager customFieldManager)
    {
        this.customFieldManager = customFieldManager;
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
        String regex = (String) args.get(Consts.REGEX);
        String msg = (String) args.get(Consts.MSG);

        if (!Utils.isValidStr(cfId) || !Utils.isValidStr(regex))
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

            Pattern pattern = Pattern.compile(regex);
            Matcher m = pattern.matcher(cfVal.toString());
            if (!m.matches())
            {
                throw new InvalidInputException(String.format("'%s': %s", customField.getName(), msg));
            }
        }
    }
}
