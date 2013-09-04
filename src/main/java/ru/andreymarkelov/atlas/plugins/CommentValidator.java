/*
 * Created by Andrey Markelov 02-10-2012.
 * Copyright Mail.Ru Group 2012. All rights reserved.
 */
package ru.andreymarkelov.atlas.plugins;

import java.util.Map;
import java.util.StringTokenizer;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.Validator;
import com.opensymphony.workflow.WorkflowException;

/**
 * Comment validator.
 */
public class CommentValidator
    implements Validator
{
    private final GroupManager groupManager;

    /**
     * Constructor.
     */
    public CommentValidator(
        GroupManager groupManager)
    {
        this.groupManager = groupManager;
    }

    @Override
    public void validate(
        Map transientVars, Map args,
        PropertySet ps)
    throws InvalidInputException, WorkflowException
    {
        try
        {
            throw new Exception();
        }
        catch (Exception e)
        {
            StackTraceElement[] stack = e.getStackTrace();
            for (StackTraceElement entry : stack)
            {
                if (entry.getClassName().equals("com.atlassian.jira.rpc.soap.JiraSoapServiceImpl") && entry.getMethodName().equals("progressWorkflowAction"))
                {
                    return;
                }
            }
        }

        String selectedGroupsListSet = (String) args.get(Consts.SELECTED_GROUPS);
        String comment = (String) transientVars.get("comment");
        if (!Utils.isValidStr(comment))
        {
            User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
            StringTokenizer st = new StringTokenizer(selectedGroupsListSet, "&");
            while (st.hasMoreTokens())
            {
                String group = st.nextToken();
                if (groupManager.getGroupNamesForUser(user).contains(group))
                {
                    return;
                }
            }
            throw new InvalidInputException(
                ComponentManager.getInstance().getJiraAuthenticationContext().getI18nHelper().getText("utils.commentseterror"));
        }
    }
}
