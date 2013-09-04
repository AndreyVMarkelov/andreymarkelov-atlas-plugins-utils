/*
 * Created by Andrey Markelov 02-10-2012.
 * Copyright Mail.Ru Group 2012. All rights reserved.
 */
package ru.andreymarkelov.atlas.plugins;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;

/**
 * Assign to last workflow step actor factory.
 *
 * @author Andrey Markelov
 */
public class AssignToStepActorPluginFactory
    extends AbstractWorkflowPluginFactory
    implements WorkflowPluginFunctionFactory
{
    @Override
    public Map<String, ?> getDescriptorParams(
        Map<String, Object> functionParams)
    {
        Map<String, Object> map = new HashMap<String, Object>();

        if (functionParams != null &&
            functionParams.containsKey(Consts.SELECTED_STATUS))
        {
            map.put(Consts.SELECTED_STATUS, extractSingleParam(functionParams, Consts.SELECTED_STATUS));
            return map;
        }

        map.put(Consts.SELECTED_STATUS, "");
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

    private Set<String> getStatusList()
    {
        Set<String> statusSet = new TreeSet<String>();

        Collection<Status> statuses = ComponentManager.getInstance().getConstantsManager().getStatusObjects();
        if (statuses != null)
        {
            for (Status status : statuses)
            {
                statusSet.add(status.getName());
            }
        }

        return statusSet;
    }

    @Override
    protected void getVelocityParamsForEdit(
        Map<String, Object> velocityParams,
        AbstractDescriptor descriptor)
    {
        velocityParams.put(Consts.ALL_STATUSES, getStatusList());
        velocityParams.put(Consts.SELECTED_STATUS, getParam(descriptor, Consts.SELECTED_STATUS));
    }

    @Override
    protected void getVelocityParamsForInput(
        Map<String, Object> velocityParams)
    {
        velocityParams.put(Consts.ALL_STATUSES, getStatusList());
        velocityParams.put(Consts.SELECTED_STATUS, "");
    }

    @Override
    protected void getVelocityParamsForView(
        Map<String, Object> velocityParams,
        AbstractDescriptor descriptor)
    {
        velocityParams.put(Consts.SELECTED_STATUS, getParam(descriptor, Consts.SELECTED_STATUS));
    }
}
