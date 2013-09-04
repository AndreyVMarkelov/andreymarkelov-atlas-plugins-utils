/*
 * Created by Andrey Markelov 02-10-2012.
 * Copyright Mail.Ru Group 2012. All rights reserved.
 */
package ru.andreymarkelov.atlas.plugins;

import java.util.HashMap;
import java.util.Map;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginValidatorFactory;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;

public class RegexCfValidatorFactory
    extends AbstractWorkflowPluginFactory
    implements WorkflowPluginValidatorFactory
{
    @Override
    public Map<String, ?> getDescriptorParams(Map<String, Object> conditionParams)
    {
        Map<String, Object> map = new HashMap<String, Object>();

        if (conditionParams != null &&
            conditionParams.containsKey(Consts.CUSTOM_FIELD_ID) &&
            conditionParams.containsKey(Consts.REGEX) &&
            conditionParams.containsKey(Consts.MSG))
        {
            map.put(Consts.CUSTOM_FIELD_ID, extractSingleParam(conditionParams, Consts.CUSTOM_FIELD_ID));
            map.put(Consts.REGEX, extractSingleParam(conditionParams, Consts.REGEX));
            map.put(Consts.MSG, extractSingleParam(conditionParams, Consts.MSG));
            return map;
        }

        map.put(Consts.CUSTOM_FIELD_ID, "");
        map.put(Consts.REGEX, "");
        map.put(Consts.MSG, "");
        return map;
    }

    private String getParam(AbstractDescriptor descriptor, String param)
    {
        if (!(descriptor instanceof ValidatorDescriptor))
        {
            throw new IllegalArgumentException("Descriptor must be a ValidatorDescriptor.");
        }

        ValidatorDescriptor validatorDescriptor = (ValidatorDescriptor) descriptor;
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
        velocityParams.put(Consts.REGEX, getParam(descriptor, Consts.REGEX));
        velocityParams.put(Consts.MSG, getParam(descriptor, Consts.MSG));
    }

    @Override
    protected void getVelocityParamsForInput(
        Map<String, Object> velocityParams)
    {
        velocityParams.put(Consts.CUSTOM_FIELD_ID, "");
        velocityParams.put(Consts.REGEX, "");
        velocityParams.put(Consts.MSG, "");
    }

    @Override
    protected void getVelocityParamsForView(
        Map<String, Object> velocityParams,
        AbstractDescriptor descriptor)
    {
        velocityParams.put(Consts.CUSTOM_FIELD_ID, getParam(descriptor, Consts.CUSTOM_FIELD_ID));
        velocityParams.put(Consts.REGEX, getParam(descriptor, Consts.REGEX));
        velocityParams.put(Consts.MSG, getParam(descriptor, Consts.MSG));
    }
}
