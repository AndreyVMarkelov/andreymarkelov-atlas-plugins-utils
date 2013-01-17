package ru.mail.jira.plugins;

import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginConditionFactory;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;

import java.util.HashMap;
import java.util.Map;

public class LimitByStatusConditionFactoryImpl extends AbstractWorkflowPluginFactory implements WorkflowPluginConditionFactory {
    public static final String STATUS_KEY = "status";
    public static final String LIMIT_KEY = "limit";

    @Override
    public Map<String, ?> getDescriptorParams(Map<String, Object> conditionParams) {
        Map<String, String> result = new HashMap<String, String>();
        result.put(STATUS_KEY, extractSingleParam(conditionParams, STATUS_KEY));
        result.put(LIMIT_KEY, extractSingleParam(conditionParams, LIMIT_KEY));
        return result;
    }

    @Override
    protected void getVelocityParamsForView(Map<String, Object> velocityParams, AbstractDescriptor descriptor) {
        if (!(descriptor instanceof ConditionDescriptor))
            throw new IllegalArgumentException("Descriptor must be a ConditionDescriptor");

        ConditionDescriptor conditionDescriptor = (ConditionDescriptor) descriptor;
        String status = (String) conditionDescriptor.getArgs().get(STATUS_KEY);
        String limit = (String) conditionDescriptor.getArgs().get(LIMIT_KEY);
        velocityParams.put(STATUS_KEY, status != null ? status : "");
        velocityParams.put(LIMIT_KEY, limit != null ? limit : "");
    }

    @Override
    protected void getVelocityParamsForInput(Map<String, Object> velocityParams) {
        velocityParams.put(STATUS_KEY, "");
        velocityParams.put(LIMIT_KEY, "");
    }

    @Override
    protected void getVelocityParamsForEdit(Map<String, Object> velocityParams, AbstractDescriptor descriptor) {
        getVelocityParamsForView(velocityParams, descriptor);
    }
}
