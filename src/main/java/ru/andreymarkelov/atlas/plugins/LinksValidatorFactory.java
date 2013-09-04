/*
 * Created by Andrey Markelov 02-10-2012.
 * Copyright Mail.Ru Group 2012. All rights reserved.
 */
package ru.andreymarkelov.atlas.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginValidatorFactory;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;

public class LinksValidatorFactory
    extends AbstractWorkflowPluginFactory
    implements WorkflowPluginValidatorFactory
{
    /**
     * Link type manager.
     */
    private final IssueLinkTypeManager iltMgr;

    /**
     * Issue type manager.
     */
    private final IssueTypeManager itMgr;

    /**
     * Project manager.
     */
    private final ProjectManager prMgr;

    /**
     * Status manager.
     */
    private final StatusManager stMgr;

    /**
     * Constructor.
     */
    public LinksValidatorFactory(
        ProjectManager prMgr,
        IssueLinkTypeManager iltMgr,
        StatusManager stMgr,
        IssueTypeManager itMgr)
    {
        this.prMgr = prMgr;
        this.iltMgr = iltMgr;
        this.stMgr = stMgr;
        this.itMgr = itMgr;
    }

    @Override
    public Map<String, ?> getDescriptorParams(
        Map<String, Object> conditionParams)
    {
        Map<String, Object> map = new HashMap<String, Object>();

        if (conditionParams != null &&
            conditionParams.containsKey("issuetype") &&
            conditionParams.containsKey("projkey") &&
            conditionParams.containsKey("linktype") &&
            conditionParams.containsKey("status"))
        {
            map.put("issuetype", extractSingleParam(conditionParams, "issuetype"));
            map.put("projkey", extractSingleParam(conditionParams, "projkey"));
            map.put("linktype", extractSingleParam(conditionParams, "linktype"));
            String[] statuses = (String[])conditionParams.get("status");
            StringBuilder sb = new StringBuilder();
            if (statuses != null)
            {
                for (String status : statuses)
                {
                    sb.append(status).append("&");
                }
            }
            map.put("status", sb.toString());
            return map;
        }

        map.put("issuetype", "");
        map.put("projkey", "");
        map.put("linktype", "");
        map.put("status", "");

        return map;
    }

    private String getParam(AbstractDescriptor descriptor, String param)
    {
        if (!(descriptor instanceof ValidatorDescriptor))
        {
            throw new IllegalArgumentException("Descriptor must be a FunctionDescriptor.");
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
        Map<String, String> prsstr = new TreeMap<String, String>();
        List<Project> prs = prMgr.getProjectObjects();
        for (Project pr : prs)
        {
            prsstr.put(pr.getKey(), pr.getName());
        }
        velocityParams.put("prsstr", prsstr);

        Map<Long, String> tpsstr = new TreeMap<Long, String>();
        Collection<IssueLinkType> tps = iltMgr.getIssueLinkTypes();
        for (IssueLinkType tp : tps)
        {
            tpsstr.put(tp.getId(), tp.getName());
        }
        velocityParams.put("tpsstr", tpsstr);

        Map<String, String> stsstr = new TreeMap<String, String>();
        Collection<Status> sts = stMgr.getStatuses();
        for (Status st : sts)
        {
            stsstr.put(st.getId(), st.getName());
        }
        velocityParams.put("stsstr", stsstr);

        Map<String, String> itsstr = new TreeMap<String, String>();
        Collection<IssueType> its = itMgr.getIssueTypes();
        for (IssueType it : its)
        {
            itsstr.put(it.getId(), it.getName());
        }
        velocityParams.put("itsstr", itsstr);

        List<String> lsts = new ArrayList<String>();
        String statuses = getParam(descriptor, "status");
        StringTokenizer st = new StringTokenizer(statuses, "&");
        while (st.hasMoreTokens())
        {
            lsts.add(st.nextToken());
        }

        velocityParams.put("projkey", getParam(descriptor, "projkey"));
        velocityParams.put("issuetype", getParam(descriptor, "issuetype"));
        velocityParams.put("linktype", getParam(descriptor, "linktype"));
        velocityParams.put("lsts", lsts);
    }

    @Override
    protected void getVelocityParamsForInput(
        Map<String, Object> velocityParams)
    {
        Map<String, String> prsstr = new TreeMap<String, String>();
        List<Project> prs = prMgr.getProjectObjects();
        for (Project pr : prs)
        {
            prsstr.put(pr.getKey(), pr.getName());
        }
        velocityParams.put("prsstr", prsstr);

        Map<Long, String> tpsstr = new TreeMap<Long, String>();
        Collection<IssueLinkType> tps = iltMgr.getIssueLinkTypes();
        for (IssueLinkType tp : tps)
        {
            tpsstr.put(tp.getId(), tp.getName());
        }
        velocityParams.put("tpsstr", tpsstr);

        Map<String, String> stsstr = new TreeMap<String, String>();
        Collection<Status> sts = stMgr.getStatuses();
        for (Status st : sts)
        {
            stsstr.put(st.getId(), st.getName());
        }
        velocityParams.put("stsstr", stsstr);

        Map<String, String> itsstr = new TreeMap<String, String>();
        Collection<IssueType> its = itMgr.getIssueTypes();
        for (IssueType it : its)
        {
            itsstr.put(it.getId(), it.getName());
        }
        velocityParams.put("itsstr", itsstr);
    }

    @Override
    protected void getVelocityParamsForView(
        Map<String, Object> velocityParams,
        AbstractDescriptor descriptor)
    {
        List<String> lsts = new ArrayList<String>();
        String statuses = getParam(descriptor, "status");
        StringTokenizer st = new StringTokenizer(statuses, "&");
        while (st.hasMoreTokens())
        {
            Status s = stMgr.getStatus(st.nextToken());
            if (s != null)
            {
                lsts.add(s.getName());
            }
        }

        String projkey = getParam(descriptor, "projkey");
        String issuetype = getParam(descriptor, "issuetype");
        String linktype = getParam(descriptor, "linktype");
        Project proj = prMgr.getProjectObjByKey(projkey);
        IssueLinkType itl = iltMgr.getIssueLinkType(Long.parseLong(linktype));
        IssueType it = itMgr.getIssueType(issuetype);

        if (proj != null)
        {
            velocityParams.put("proj", proj.getName());
        }

        if (it != null)
        {
            velocityParams.put("issuetype", it.getName());
        }

        if (itl != null)
        {
            velocityParams.put("linktype", itl.getName());
        }

        velocityParams.put("lsts", lsts);
    }
}
