/*
 * Created by Andrey Markelov 02-10-2012.
 * Copyright Mail.Ru Group 2012. All rights reserved.
 */
package ru.andreymarkelov.atlas.plugins;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginValidatorFactory;
import com.atlassian.jira.security.groups.GroupManager;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;

/**
 * Comment validator factory.
 * 
 * @author Andrey Markelov
 */
public class CommentValidatorFactory
    extends AbstractWorkflowPluginFactory
    implements WorkflowPluginValidatorFactory
{
    /**
     * Group manager.
     */
    private final GroupManager groupManager;

    /**
     * Constructor.
     */
    public CommentValidatorFactory(
        GroupManager groupManager)
    {
        this.groupManager = groupManager;
    }

    public Map<String, Object> getDescriptorParams(
        Map<String, Object> conditionParams)
    {
        Map<String, Object> map = new HashMap<String, Object>();

        if (conditionParams != null &&
            conditionParams.containsKey(Consts.SELECTED_GROUPS))
        {
            map.put(Consts.SELECTED_GROUPS, extractSingleParam(conditionParams, Consts.SELECTED_GROUPS));
            return map;
        }

        map.put(Consts.SELECTED_GROUPS, "");
        return map;
    }

    /**
     * Get parameter.
     */
    private String getParam(
        AbstractDescriptor descriptor,
        String param)
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

    private Set<String> getSetParams(
        AbstractDescriptor descriptor,
        String param)
    {
        Set<String> params = new TreeSet<String>();

        String paramStr = getParam(descriptor, param);
        if (paramStr != null)
        {
            StringTokenizer st = new StringTokenizer(paramStr, "&");
            while (st.hasMoreTokens())
            {
                params.add(st.nextToken());
            }
        }

        return params;
    }

    @Override
    protected void getVelocityParamsForEdit(
        Map<String, Object> velocityParams,
        AbstractDescriptor descriptor)
    {
        Collection<Group> groups = groupManager.getAllGroups();
        velocityParams.put(Consts.ALL_GROUPS, Collections.unmodifiableCollection(groups));
        velocityParams.put(Consts.SELECTED_GROUPS_SET, getSetParams(descriptor, Consts.SELECTED_GROUPS));
    }

    @Override
    protected void getVelocityParamsForInput(
        Map<String, Object> velocityParams)
    {
        Collection<Group> groups = groupManager.getAllGroups();
        velocityParams.put(Consts.ALL_GROUPS, Collections.unmodifiableCollection(groups));
        velocityParams.put(Consts.SELECTED_GROUPS_SET, new TreeSet<String>());
    }

    @Override
    protected void getVelocityParamsForView(
        Map<String, Object> velocityParams,
        AbstractDescriptor descriptor)
    {
        velocityParams.put(Consts.SELECTED_GROUPS, getSetParams(descriptor, Consts.SELECTED_GROUPS));
    }
}
