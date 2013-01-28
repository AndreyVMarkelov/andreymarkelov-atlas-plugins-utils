package ru.mail.jira.plugins;


import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginValidatorFactory;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;


public class SingularStatusValidatorFactory extends
        AbstractWorkflowPluginFactory implements WorkflowPluginValidatorFactory
{
    @Override
    public Map<String, ?> getDescriptorParams(
        Map<String, Object> conditionParams)
    {
        Map<String, Object> map = new HashMap<String, Object>();

        if (conditionParams != null
            && conditionParams.containsKey(Consts.ISSUE_TYPE)
            && conditionParams.containsKey(Consts.ISSUE_STATUS))
        {
            map.put(Consts.ISSUE_TYPE,
                extractSingleParam(conditionParams, Consts.ISSUE_TYPE));
            map.put(Consts.ISSUE_STATUS,
                extractSingleParam(conditionParams, Consts.ISSUE_STATUS));
            return map;
        }

        map.put(Consts.ISSUE_TYPE, "");
        map.put(Consts.ISSUE_STATUS, "");
        return map;
    }

    private String getParam(AbstractDescriptor descriptor, String param)
    {
        if (!(descriptor instanceof ValidatorDescriptor))
        {
            throw new IllegalArgumentException(
                "Descriptor must be a FunctionDescriptor.");
        }

        ValidatorDescriptor validatorDescriptor = (ValidatorDescriptor) descriptor;
        String value = (String) validatorDescriptor.getArgs().get(param);

        if (value != null && value.trim().length() > 0)
        {
            return value;
        }
        else
        {
            return "";
        }
    }

    @Override
    protected void getVelocityParamsForEdit(Map<String, Object> velocityParams,
        AbstractDescriptor descriptor)
    {
        velocityParams.put(Consts.ISSUE_TYPE,
            getParam(descriptor, Consts.ISSUE_TYPE));
        velocityParams.put(Consts.ISSUE_STATUS,
            getParam(descriptor, Consts.ISSUE_STATUS));
    }

    @Override
    protected void getVelocityParamsForInput(Map<String, Object> velocityParams)
    {
        velocityParams.put(Consts.ISSUE_TYPE, "");
        velocityParams.put(Consts.ISSUE_STATUS, "");
    }

    @Override
    protected void getVelocityParamsForView(Map<String, Object> velocityParams,
        AbstractDescriptor descriptor)
    {
        velocityParams.put(Consts.ISSUE_TYPE,
            getParam(descriptor, Consts.ISSUE_TYPE));
        velocityParams.put(Consts.ISSUE_STATUS,
            getParam(descriptor, Consts.ISSUE_STATUS));
    }
}
