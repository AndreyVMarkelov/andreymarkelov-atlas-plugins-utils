/*
 * Created by Andrey Markelov 02-10-2012.
 * Copyright Mail.Ru Group 2012. All rights reserved.
 */
package ru.andreymarkelov.atlas.plugins;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.changehistory.ChangeHistoryItem;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;

/**
 * Assign issue to actor who performed step with destination status.
 *
 * @author Andrey Markelov
 */
public class AssignToStepActorFunction
    extends AbstractJiraFunctionProvider
{
    @Override
    public void execute(
        Map transientVars,
        Map args,
        PropertySet ps)
    throws WorkflowException
    {
        //--> current status
        MutableIssue issue = getIssue(transientVars);
        //--> selected status
        String status = (String) args.get(Consts.SELECTED_STATUS);

        String assignee = "";
        Timestamp changeTs = null;
        List<ChangeHistoryItem> items = ComponentManager.getInstance().getChangeHistoryManager().getAllChangeItems(issue);
        if (items != null)
        {
            for (ChangeHistoryItem chi : items)
            {
                if (chi.getField().equals("status"))
                {
                    Map<String, String> map = chi.getTos();
                    for (Map.Entry<String, String> entry : map.entrySet())
                    {
                        if (entry.getValue().equals(status))
                        {
                            if (changeTs == null)
                            {
                                changeTs = chi.getCreated();
                                assignee = chi.getUser();
                            }
                            else
                            {
                                if (chi.getCreated().after(changeTs))
                                {
                                    changeTs = chi.getCreated();
                                    assignee = chi.getUser();
                                }
                            }
                        }
                    }
                }
            }
        }

        if (assignee.length() > 0)
        {
            issue.setAssigneeId(assignee);
        }
    }
}
