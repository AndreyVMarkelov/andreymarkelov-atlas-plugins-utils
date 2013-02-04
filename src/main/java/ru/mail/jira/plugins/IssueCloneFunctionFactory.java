/*
 * Created by Andrey Markelov 02-02-2013.
 * Copyright Mail.Ru Group 2013. All rights reserved.
 */
package ru.mail.jira.plugins;

import java.util.HashMap;
import java.util.Map;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
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

    @Override
    protected void getVelocityParamsForEdit(
        Map<String, Object> velocityParams,
        AbstractDescriptor descriptor)
    {
        velocityParams.put(Consts.ISSUE_CLONE_COUNT, getParam(descriptor, Consts.ISSUE_CLONE_COUNT));
        velocityParams.put(Consts.ISSUE_CLONE_ATTACHMENTS, getParam(descriptor, Consts.ISSUE_CLONE_ATTACHMENTS));
        velocityParams.put(Consts.ISSUE_CLONE_LINKS, getParam(descriptor, Consts.ISSUE_CLONE_LINKS));
    }

    @Override
    protected void getVelocityParamsForInput(
        Map<String, Object> velocityParams)
    {
        velocityParams.put(Consts.ISSUE_CLONE_COUNT, "0");
        velocityParams.put(Consts.ISSUE_CLONE_ATTACHMENTS, Boolean.TRUE);
        velocityParams.put(Consts.ISSUE_CLONE_LINKS, Boolean.TRUE);
    }

    @Override
    protected void getVelocityParamsForView(
        Map<String, Object> velocityParams,
        AbstractDescriptor descriptor)
    {
        velocityParams.put(Consts.ISSUE_CLONE_COUNT, getParam(descriptor, Consts.ISSUE_CLONE_COUNT));
        velocityParams.put(Consts.ISSUE_CLONE_ATTACHMENTS, getParam(descriptor, Consts.ISSUE_CLONE_ATTACHMENTS));
        velocityParams.put(Consts.ISSUE_CLONE_LINKS, getParam(descriptor, Consts.ISSUE_CLONE_LINKS));
    }
}
