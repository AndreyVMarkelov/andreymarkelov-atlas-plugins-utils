/*
 * Created by Andrey Markelov 02-10-2012. Copyright Mail.Ru Group 2012. All
 * rights reserved.
 */
package ru.andreymarkelov.atlas.plugins;

import java.util.Map;
import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.WorkflowException;


/**
 * Estimate function.
 * 
 * @author Andrey Markelov
 */
public class EstimatePostFunction extends AbstractJiraFunctionProvider
{
    /**
     * Custom field manager.
     */
    private final CustomFieldManager cfMgr;

    /**
     * Constructor.
     */
    public EstimatePostFunction(CustomFieldManager cfMgr)
    {
        this.cfMgr = cfMgr;
    }

    @Override
    public void execute(Map transientVars, Map args, PropertySet ps)
        throws WorkflowException
    {
        MutableIssue issue = getIssue(transientVars);

        String cfId = (String) args.get(Consts.CUSTOM_FIELD_ID);

        if (!Utils.isValidStr(cfId))
        {
            return;
        }

        try
        {
            Long.parseLong(cfId);
        }
        catch (NumberFormatException nex)
        {
            return;
        }

        CustomField customField = cfMgr.getCustomFieldObject(Long
            .parseLong(cfId));
        if (customField != null)
        {
            JiraAuthenticationContext authCtx = ComponentManager.getInstance()
                .getJiraAuthenticationContext();
            I18nHelper i18nHelper = authCtx.getI18nHelper();

            Object cfVal = issue.getCustomFieldValue(customField);
            if (cfVal == null)
            {
                throw new InvalidInputException(i18nHelper.getText(
                    "utils.estimatepf.error.fieldrequired",
                    customField.getName()));
            }

            String cfStrVal = cfVal.toString();
            long originalEstimate;
            try
            {
                originalEstimate = ComponentManager
                    .getInstance()
                    .getJiraDurationUtils()
                    .parseDuration(
                        cfStrVal,
                        ComponentManager.getInstance()
                            .getJiraAuthenticationContext().getLocale());
            }
            catch (InvalidDurationException e)
            {
                throw new InvalidInputException(
                    i18nHelper.getText("utils.estimatepf.error.invalidformat"));
            }
            if (originalEstimate <= 0)
            {
                throw new InvalidInputException(
                    i18nHelper.getText("utils.estimatepf.error.isnonpositive"));
            }
            issue.setOriginalEstimate(originalEstimate);
            issue.setEstimate(originalEstimate);
        }
    }
}
