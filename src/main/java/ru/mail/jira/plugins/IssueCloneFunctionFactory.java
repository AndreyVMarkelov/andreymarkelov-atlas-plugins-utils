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
            functionParams.containsKey(Consts.CUSTOM_FIELD_ID))
        {
            map.put(Consts.CUSTOM_FIELD_ID, extractSingleParam(functionParams, Consts.CUSTOM_FIELD_ID));
            return map;
        }

        map.put(Consts.CUSTOM_FIELD_ID, "");
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
        velocityParams.put(Consts.CUSTOM_FIELD_ID, getParam(descriptor, Consts.CUSTOM_FIELD_ID));
    }

    @Override
    protected void getVelocityParamsForInput(
        Map<String, Object> velocityParams)
    {
        velocityParams.put(Consts.CUSTOM_FIELD_ID, "");
    }

    @Override
    protected void getVelocityParamsForView(
        Map<String, Object> velocityParams,
        AbstractDescriptor descriptor)
    {
        velocityParams.put(Consts.CUSTOM_FIELD_ID, getParam(descriptor, Consts.CUSTOM_FIELD_ID));
    }
}
