/*
 * Created by Andrey Markelov 02-02-2013.
 * Copyright Mail.Ru Group 2013. All rights reserved.
 */
package ru.mail.jira.plugins;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.atlassian.jira.project.Project;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;

/**
 * Issue Clone Function Factory.
 * 
 * @author Andrey Markelov
 */
public class IssueCloneFunctionFactory
    extends AbstractWorkflowPluginFactory
    implements WorkflowPluginFunctionFactory
{
    @Override
    public Map<String, ?> getDescriptorParams(
        Map<String, Object> functionParams)
    {
        Map<String, Object> map = new HashMap<String, Object>();

        if (functionParams != null &&
            functionParams.containsKey(Consts.ISSUE_PROJECT))
        {
            map.put(Consts.ISSUE_PROJECT, extractSingleParam(functionParams, Consts.ISSUE_PROJECT));
        }
        else
        {
            functionParams.put(Consts.ISSUE_PROJECT, "");
        }

        if (functionParams != null &&
            functionParams.containsKey(Consts.CLONE_PREFIX))
        {
            map.put(Consts.CLONE_PREFIX, extractSingleParam(functionParams, Consts.CLONE_PREFIX));
        }
        else
        {
            functionParams.put(Consts.CLONE_PREFIX, "");
        }

        if (functionParams != null &&
            functionParams.containsKey(Consts.CLONE_ASSIGNEE))
        {
            map.put(Consts.CLONE_ASSIGNEE, extractSingleParam(functionParams, Consts.CLONE_ASSIGNEE));
        }
        else
        {
            functionParams.put(Consts.CLONE_ASSIGNEE, "");
        }

        if (functionParams != null &&
            functionParams.containsKey(Consts.CLONE_LINK_TYPE))
        {
            map.put(Consts.CLONE_LINK_TYPE, extractSingleParam(functionParams, Consts.CLONE_LINK_TYPE));
        }
        else
        {
            functionParams.put(Consts.CLONE_LINK_TYPE, "");
        }

        if (functionParams != null &&
            functionParams.containsKey(Consts.ISSUE_TYPE))
        {
            map.put(Consts.ISSUE_TYPE, extractSingleParam(functionParams, Consts.ISSUE_TYPE));
        }
        else
        {
            functionParams.put(Consts.ISSUE_TYPE, "");
        }

        if (functionParams != null &&
            functionParams.containsKey(Consts.ISSUE_CLONE_COUNT))
        {
            map.put(Consts.ISSUE_CLONE_COUNT, extractSingleParam(functionParams, Consts.ISSUE_CLONE_COUNT));
        }
        else
        {
            functionParams.put(Consts.ISSUE_CLONE_COUNT, 0);
        }

        if (functionParams != null &&
            functionParams.containsKey(Consts.ISSUE_CLONE_ATTACHMENTS))
        {
             map.put(Consts.ISSUE_CLONE_ATTACHMENTS, Boolean.TRUE);
        }
        else
        {
            map.put(Consts.ISSUE_CLONE_ATTACHMENTS, Boolean.FALSE);
        }

        if (functionParams != null &&
            functionParams.containsKey(Consts.ISSUE_CLONE_LINKS))
        {
            map.put(Consts.ISSUE_CLONE_LINKS, Boolean.TRUE);
        }
        else
        {
            map.put(Consts.ISSUE_CLONE_LINKS, Boolean.FALSE);
        }

        return map;
    }

    /**
     * Get all issue types.
     */
    private Map<String, String> getIssueTypes()
    {
        Map<String, String> projs = new TreeMap<String, String>();

        Collection<IssueType> its = ComponentManager.getInstance().getConstantsManager().getAllIssueTypeObjects();
        if (its != null)
        {
            for (IssueType it : its)
            {
                projs.put(it.getId(), it.getName());
            }
        }

        return projs;
    }

    /**
     * Get parameter.
     */
    private String getParam(
        AbstractDescriptor descriptor,
        String param)
    {
        if (!(descriptor instanceof FunctionDescriptor))
        {
            throw new IllegalArgumentException("Descriptor must be a FunctionDescriptor.");
        }

        FunctionDescriptor validatorDescriptor = (FunctionDescriptor) descriptor;
        String value = (String) validatorDescriptor.getArgs().get(param);

        if (value!=null && value.trim().length() > 0)
        {
            return value;
        }
        else 
        {
            return "";
        }
    }

    /**
     * Get all projects.
     */
    private Map<Long, String> getProjects()
    {
        Map<Long, String> projs = new TreeMap<Long, String>();

        List<Project> projObjs = ComponentManager.getInstance().getProjectManager().getProjectObjects();
        if (projObjs != null)
        {
            for (Project projObj : projObjs)
            {
                projs.put(projObj.getId(), projObj.getName());
            }
        }

        return projs;
    }

    @Override
    protected void getVelocityParamsForEdit(
        Map<String, Object> velocityParams,
        AbstractDescriptor descriptor)
    {
        velocityParams.put(Consts.ISSUE_CLONE_COUNT, getParam(descriptor, Consts.ISSUE_CLONE_COUNT));
        velocityParams.put(Consts.ISSUE_CLONE_ATTACHMENTS, getParam(descriptor, Consts.ISSUE_CLONE_ATTACHMENTS));
        velocityParams.put(Consts.ISSUE_CLONE_LINKS, getParam(descriptor, Consts.ISSUE_CLONE_LINKS));
        velocityParams.put(Consts.ISSUE_PROJECT, getParam(descriptor, Consts.ISSUE_PROJECT));
        velocityParams.put(Consts.ISSUE_TYPE, getParam(descriptor, Consts.ISSUE_TYPE));
        velocityParams.put(Consts.CLONE_PREFIX, getParam(descriptor, Consts.CLONE_PREFIX));
        velocityParams.put(Consts.CLONE_ASSIGNEE, getParam(descriptor, Consts.CLONE_ASSIGNEE));
        velocityParams.put("its", getIssueTypes());
        velocityParams.put("allProjects", getProjects());
    }

    @Override
    protected void getVelocityParamsForInput(
        Map<String, Object> velocityParams)
    {
        velocityParams.put(Consts.ISSUE_CLONE_COUNT, "0");
        velocityParams.put(Consts.ISSUE_CLONE_ATTACHMENTS, Boolean.TRUE);
        velocityParams.put(Consts.ISSUE_CLONE_LINKS, Boolean.TRUE);
        velocityParams.put(Consts.ISSUE_PROJECT, "");
        velocityParams.put(Consts.ISSUE_TYPE, "");
        velocityParams.put(Consts.CLONE_PREFIX, "");
        velocityParams.put(Consts.CLONE_ASSIGNEE, "");
        velocityParams.put("its", getIssueTypes());
        velocityParams.put("allProjects", getProjects());
    }

    @Override
    protected void getVelocityParamsForView(
        Map<String, Object> velocityParams,
        AbstractDescriptor descriptor)
    {
        velocityParams.put(Consts.ISSUE_CLONE_COUNT, getParam(descriptor, Consts.ISSUE_CLONE_COUNT));
        velocityParams.put(Consts.ISSUE_CLONE_ATTACHMENTS, getParam(descriptor, Consts.ISSUE_CLONE_ATTACHMENTS));
        velocityParams.put(Consts.ISSUE_CLONE_LINKS, getParam(descriptor, Consts.ISSUE_CLONE_LINKS));
        velocityParams.put(Consts.CLONE_PREFIX, getParam(descriptor, Consts.CLONE_PREFIX));
        velocityParams.put(Consts.CLONE_ASSIGNEE, getParam(descriptor, Consts.CLONE_ASSIGNEE));

        String projId = getParam(descriptor, Consts.ISSUE_PROJECT);
        if (Utils.isValidStr(projId))
        {
            Project proj = ComponentManager.getInstance().getProjectManager().getProjectObj(Long.parseLong(projId));
            if (proj != null)
            {
                velocityParams.put("proj", proj.getName());
            }
            else
            {
                velocityParams.put("proj", "");
            }
        }
        else
        {
            velocityParams.put("proj", "");
        }

        String issueTypeId = getParam(descriptor, Consts.ISSUE_TYPE);
        if (Utils.isValidStr(issueTypeId))
        {
            IssueType it = ComponentManager.getInstance().getConstantsManager().getIssueTypeObject(issueTypeId);
            if (it != null)
            {
                velocityParams.put("it", it.getName());
            }
            else
            {
                velocityParams.put("it", "");
            }
        }
        else
        {
            velocityParams.put("it", "");
        }
    }
}
