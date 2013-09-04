package ru.andreymarkelov.atlas.plugins;

import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.impl.GenericTextCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;

/**
 * Estimate value custom field.
 * 
 * @author Andrey Markelov
 */
public class EstimateValueCf
    extends GenericTextCFType
{
    public interface Visitor<T> extends VisitorBase<T>
    {
        T visinEstimateValue(EstimateValueCf estimateValueCf);
    }

    /**
     * Constructor.
     */
    public EstimateValueCf(
        CustomFieldValuePersister customFieldValuePersister,
        GenericConfigManager genericConfigManager)
    {
        super(customFieldValuePersister, genericConfigManager);
    }

    @Override
    public Object accept(
        VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visinEstimateValue(this);
        }

        return super.accept(visitor);
    }

    public String getSingularObjectFromString(
        final String string)
    throws FieldValidationException
    {
        ComponentManager.getInstance().getJiraAuthenticationContext();
        final String estimateTime = (string == null) ? null : string.trim();

        try
        {
            ComponentManager.getInstance().getJiraDurationUtils().parseDuration(estimateTime, getI18nBean().getLocale());
        }
        catch (InvalidDurationException e)
        {
            throw new FieldValidationException(getI18nBean().getText("utils.estimatepf.error.invalidformat"));
        }

        return estimateTime;
    }
}
